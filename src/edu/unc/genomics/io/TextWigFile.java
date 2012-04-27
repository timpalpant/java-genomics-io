package edu.unc.genomics.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;
import org.broad.igv.bbfile.WigItem;

import ed.javatools.BufferedRandomAccessFile;
import edu.ucsc.genome.TrackHeader;
import edu.ucsc.genome.TrackHeaderException;
import edu.unc.genomics.util.ChecksumUtils;

public class TextWigFile extends WigFile {
	private static final long serialVersionUID = 3L;
	public static final String INDEX_EXTENSION = ".idx";
	public static final int KEY_GRANULARITY = 10_000;
	
	private static Logger log = Logger.getLogger(TextWigFile.class);
	
	private BufferedRandomAccessFile raf;
	private TrackHeader header = new TrackHeader("wiggle_0");
	
	private List<Contig> contigs = new ArrayList<Contig>();
	private Set<String> chromosomes = new HashSet<String>();
	
	private long checksum;
	
	private SummaryStatistics stats;

	/**
	 * @param p the Path to the Wig file
	 * @throws IOException if an error occurs while opening or reading from the Wig file
	 * @throws WigFileException if an error occurs while indexing the Wig file
	 */
	public TextWigFile(Path p) throws IOException, WigFileException {
		super(p);
		raf = new BufferedRandomAccessFile(p.toFile(), "r");

		String headerLine = raf.readLine();
		if (headerLine.startsWith("track")) {
			try {
				header = TrackHeader.parse(headerLine);
			} catch (TrackHeaderException e) {
				log.error("Error parsing UCSC track header in file: " + p.toString());
				e.printStackTrace();
			}
		}

		// Compute the checksum of this file
		checksum = ChecksumUtils.adler32(p);
		
		// Attempt to load an index from disk, or generate one otherwise
		Path indexFile = p.resolveSibling(p.getFileName()+INDEX_EXTENSION);
		try {
			loadIndex(indexFile, true);
		} catch (IOException | WigFileException e) {
			// (Re)generate if the index could not be loaded
			Files.deleteIfExists(indexFile);
			generateIndex();
			saveIndex(indexFile);
		}
	}
	
	/**
	 * Specify an index and forego matching checksum
	 * @param p the path to the Wig file
	 * @param index the path for the Wig file's precomputed index
	 * @throws IOException if an error occurs opening the Wig file or its index
	 * @throws WigFileException if an error ocurs while loading the index
	 */
	public TextWigFile(Path p, Path index) throws IOException, WigFileException {
		super(p);
		raf = new BufferedRandomAccessFile(p.toFile(), "r");

		String headerLine = raf.readLine();
		if (headerLine.startsWith("track")) {
			try {
				header = TrackHeader.parse(headerLine);
			} catch (TrackHeaderException e) {
				System.err.println("Error parsing UCSC track header in file: " + p.toString());
				System.err.println(e.getMessage());
			}
		}
		
		loadIndex(index, false);
	}
	
	@Override
	public void close() {
		try {
			raf.close();
		} catch (IOException e) { 
			throw new RuntimeException("Error closing TextWigFile");
		}
	}

	@Override
	public Iterator<WigItem> query(String chr, int low, int high) throws IOException, WigFileException {
		if (!includes(chr, low, high)) {
			throw new WigFileException("WigFile does not contain data for region: " + chr + ":" + low + "-" + high);
		} else if (low > high) {
			throw new WigFileException("Query start > stop!");
		}
		
		List<Contig> relevantContigs = getContigsForQuery(chr, low, high);
		return new TextWigIterator(raf, relevantContigs.iterator(), chr, low, high);
	}
	
	private List<Contig> getContigsForQuery(String chr, int low, int high) {
		List<Contig> relevantContigs = new ArrayList<Contig>();
		
		for (Contig c : contigs) {
			if (c.getChr().equals(chr) && c.getStop() >= low && c.getStart() <= high) {
				relevantContigs.add(c);
			}
		}
		
		return relevantContigs;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("ASCII Text Wiggle file: " + header.toString() + "\n");
		
		s.append("Chromosomes:\n");
		for (String chr : chromosomes) {
			s.append('\t').append(chr).append(" start=").append(getChrStart(chr)).append(" stop=").append(getChrStop(chr)).append('\n');
		}
		
		s.append("Contigs:\n");
		for (Contig c : contigs) {
			s.append("\t").append(c.toString()).append('\n');
		}
		
		s.append("Basic Statistics:\n");
		s.append("\tMean:\t\t\t").append(mean()).append("\n");
		s.append("\tStandard Deviation:\t").append(stdev()).append("\n");
		s.append("\tTotal:\t\t\t").append(total()).append("\n");
		s.append("\tBases Covered:\t\t").append(numBases()).append("\n");
		s.append("\tMin value:\t\t").append(min()).append("\n");
		s.append("\tMax value:\t\t").append(max());
		
		return s.toString();
	}
	
	@Override
	public Set<String> chromosomes() {
		return chromosomes;
	}
	
	@Override
	public int getChrStart(String chr) {
		if (!includes(chr)) {
			return -1;
		}
		
		int start = Integer.MAX_VALUE;
		for (Contig c : contigs) {
			if (c.getChr().equals(chr) && c.getStart() < start) {
				start = c.getStart();
			}
		}
		
		return start;
	}
	
	@Override
	public int getChrStop(String chr) {		
		int stop = -1;
		for (Contig c : contigs) {
			if (c.getChr().equals(chr) && c.getStop() > stop) {
				stop = c.getStop();
			}
		}
		
		return stop;
	}
	
	@Override
	public boolean includes(String chr, int start, int stop) {
		return includes(chr) && getChrStart(chr) <= start && getChrStop(chr) >= stop;
	}
	
	@Override
	public boolean includes(String chr) {
		return chromosomes.contains(chr);
	}
	
	/**
	 * @return the numBases
	 */
	@Override
	public long numBases() {
		return stats.getN();
	}
	
	/**
	 * @return the total
	 */
	@Override
	public double total() {
		return stats.getSum();
	}
	
	/**
	 * @return the mean
	 */
	@Override
	public double mean() {
		return stats.getMean();
	}
	
	/**
	 * @return the stdev
	 */
	@Override
	public double stdev() {
		return Math.sqrt(stats.getPopulationVariance());
	}
	
	/**
	 * @return the min
	 */
	@Override
	public double min() {
		return stats.getMin();
	}
	
	/**
	 * @return the max
	 */
	@Override
	public double max() {
		return stats.getMax();
	}
	
	/**
	 * Index this WigFile
	 * @throws IOException 
	 * @throws WigFileException 
	 */
	private void generateIndex() throws IOException, WigFileException {
		log.debug("Indexing ascii text Wig file: " + p.getFileName().toString());
		
		// Skip the track line, if there is one
		raf.seek(0);
		long lineNum = 0;
		String line = raf.readLine();
		if (!line.startsWith("track")) {
			raf.seek(0);
		} else {
			lineNum++;
		}

		// Index the Contigs and data in the Wig File by going through it once
		stats = new SummaryStatistics();
		contigs = new ArrayList<Contig>();
		Contig contig = null;
		int bp = 0;
		double value;
		long cursor = raf.getFilePointer();
		while ((line = raf.readLine()) != null) {
			lineNum++;
			
			if (line.startsWith(Contig.FIXED_STEP) || line.startsWith(Contig.VARIABLE_STEP)) {
				// If this is the end of a previous Contig, store the stop info
				if (contigs.size() > 0) {
					contig.setStopLine(lineNum-1);
					contig.setStop(bp + contig.getSpan() - 1);
				}
				
				// Now parse the new Contig
				contig = Contig.parse(line);
				contigs.add(contig);
				
				// Set the new Contig's start info
				contig.setStartLine(lineNum+1);
				if (contig instanceof VariableStepContig) {
					cursor = raf.getFilePointer();
					String firstLine = raf.readLine();
					int delim = firstLine.indexOf('\t');
					if (delim == -1) {
						throw new WigFileException("Illegal format in variableStep contig, line " + lineNum);
					}
					try {
						bp = Integer.parseInt(firstLine.substring(0, delim));
					} catch (NumberFormatException e) {
						throw new WigFileException("Illegal format in variableStep contig, line " + lineNum);
					}
					contig.setStart(bp);
					raf.seek(cursor);
				} else {
					bp = contig.getStart() - ((FixedStepContig)contig).getStep();
				}
			} else {
				if (contig instanceof FixedStepContig) {
					bp += ((FixedStepContig)contig).getStep();
					try {
						value = Double.parseDouble(line);
					} catch (NumberFormatException e) {
						throw new WigFileException("Illegal format in fixedStep contig, line " + lineNum);
					}
				} else {
					int delim = line.indexOf('\t');
					if (delim == -1) {
						throw new WigFileException("Illegal format in variableStep contig, line " + lineNum);
					}
					
					try {
						bp = Integer.parseInt(line.substring(0, delim));
						value = Double.parseDouble(line.substring(delim+1));
					} catch (NumberFormatException e) {
						throw new WigFileException("Illegal format in variableStep contig, line " + lineNum);
					}
				}
				
				if (!Double.isNaN(value) && !Double.isInfinite(value)) {
					for (int i = 0; i < contig.getSpan(); i++) {
						stats.addValue(value);
					}
				}
				
				// Store this line in the index
				if ((lineNum - contig.getStartLine()) % KEY_GRANULARITY == 0) {
					contig.storeIndex(bp, cursor);
				}
			}
			
			// Store the cursor position if the next line will be stored in the index
			if ((lineNum + 1 - contig.getStartLine()) % KEY_GRANULARITY == 0) {
				cursor = raf.getFilePointer();
			}
		}
		
		// Set the stop info for the last contig
		if (contigs.size() > 0) {
			contig.setStopLine(lineNum);
			contig.setStop(bp + contig.getSpan() - 1);
		}

		// Set the Set of chromosomes
		chromosomes = new HashSet<String>();
		for (Contig c : contigs) {
			chromosomes.add(c.getChr());
		}
	}
	
	/**
	 * Load information about this Wig file from a saved index
	 * @param p
	 * @throws IOException 
	 * @throws WigFileException 
	 */
	private void loadIndex(Path p, boolean matchChecksum) throws IOException, WigFileException {
		log.debug("Attempting to load Wig file index from disk");
		try (InputStream is = Files.newInputStream(p)) {
			BufferedInputStream bis = new BufferedInputStream(is);
			ObjectInputStream dis = new ObjectInputStream(bis);
			
			// Load and match version
			long version = dis.readLong();
			if (version != serialVersionUID) {
				log.error("Version of index does not match version of Wig file!");
				throw new WigFileException("Cannot load index from older version!");
			}
			// Load and optionally match checksum
			long indexChecksum = dis.readLong();
			if (matchChecksum && indexChecksum != checksum) {
				log.error("Index does not match checksum of Wig file!");
				throw new WigFileException("Index does not match checksum of Wig file!");
			}
			
			// Load statistics
			try {
				stats = (SummaryStatistics) dis.readObject();
			} catch (ClassNotFoundException e) {
				log.error("ClassNotFoundException while loading Wig statistics from index file");
				e.printStackTrace();
				throw new WigFileException("ClassNotFoundException while trying to load Wig statistics from index file");
			}
			
			try {
				// Load chromosomes
				int numChromosomes = dis.readInt();
				chromosomes = new HashSet<String>(numChromosomes);
				for (int i = 0; i < numChromosomes; i++) {
					String chr = (String) dis.readObject();
					chromosomes.add(chr);
				}
				
				// Load Contigs
				int numContigs = dis.readInt();
				contigs = new ArrayList<Contig>(numContigs);
				for (int i = 0; i < numContigs; i++) {
					Contig contig = (Contig) dis.readObject();
					contigs.add(contig);
				}
				
				dis.close();
				bis.close();
			} catch (ClassNotFoundException e) {
				log.error("ClassNotFoundException while loading Wig index from file");
				e.printStackTrace();
				throw new WigFileException("ClassNotFoundException while trying to load Wig index from file");
			}
		}
	}
	
	/**
	 * Save the statistics and data index about this Wig file to disk
	 * @param p
	 * @throws IOException 
	 */
	private void saveIndex(Path p) throws IOException {
		log.debug("Writing Wig index information to disk");
		try (OutputStream os = Files.newOutputStream(p)) {
			BufferedOutputStream bos = new BufferedOutputStream(os);
			ObjectOutputStream dos = new ObjectOutputStream(bos);
			
			// Write the serialization version and corresponding Wig file checksum
			// at the top so it can easily be matched
			dos.writeLong(serialVersionUID);
			dos.writeLong(checksum);
			
			// Write statistics
			dos.writeObject(stats);
			
			// Write chromosomes
			dos.writeInt(chromosomes.size());
			for (String chr : chromosomes) {
				dos.writeObject(chr);
			}
			
			// Write Contigs
			dos.writeInt(contigs.size());
			for (Contig c : contigs) {
				dos.writeObject(c);
			}
			
			dos.close();
			bos.close();
		} catch (IOException e) {
			log.error("Error saving Wig index information to disk!: " + e.getMessage());
			e.printStackTrace();
			// Remove the file because it's probably corrupt
			Files.deleteIfExists(p);
		}
	}
	
	/**
	 * Takes an iterator of Contigs that are relevant to a query,
	 * and a file with data for those contigs, and iterates over all WigItems
	 * in the resulting query across all relevant Contigs
	 * 
	 * @author timpalpant
	 *
	 */
	private static class TextWigIterator implements Iterator<WigItem> {

		private final RandomAccessFile raf;
		private final String chr;
		private final int start;
		private final int stop;
		private final Iterator<Contig> relevantContigsIter;
		private Iterator<WigItem> currentContigIter;
		
		public TextWigIterator(final RandomAccessFile raf, final Iterator<Contig> relevantContigsIter, 
				final String chr, final int start, final int stop) {
			this.raf = raf;
			this.chr = chr;
			this.relevantContigsIter = relevantContigsIter;
			this.start = start;
			this.stop = stop;
		}
		
		@Override
		public boolean hasNext() {
			// If there is no current contig, or there are no more entries in the current contig
			if (currentContigIter == null || !currentContigIter.hasNext()) {
				return advanceContig();
			}
			
			// The currentContigIter is not null, and it hasNext
			return true;
		}

		@Override
		public WigItem next() {
			if (hasNext()) {
				return currentContigIter.next();
			}
			
			throw new NoSuchElementException("No more WigItem elements available");
		}

		@Override
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Cannot remove records from Wig file");
		}
		
		private boolean advanceContig() {
			while (relevantContigsIter.hasNext()) {
				Contig currentContig = relevantContigsIter.next();
				try {
					currentContigIter = currentContig.query(raf, chr, start, stop);
					if (currentContigIter.hasNext()) {
						return true;
					}
				} catch (IOException | WigFileException e) {
					log.error("Error querying Contig: " + currentContig.toString());
					e.printStackTrace();
					throw new RuntimeException("Error querying Contig: " + currentContig.toString());
				}
			}
			
			return false;
		}
	}
}

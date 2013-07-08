package edu.unc.genomics.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;

import ed.javatools.BufferedRandomAccessFile;
import edu.ucsc.genome.TrackHeader;
import edu.ucsc.genome.TrackHeaderException;
import edu.unc.genomics.Contig;
import edu.unc.genomics.Interval;
import edu.unc.genomics.util.ChecksumUtils;

/**
 * An ASCII-text Wiggle file. For more information, see: http://genome.ucsc.edu/goldenPath/help/wiggle.html
 * 
 * BigWig and regular ASCII-text Wig files may be used interchangeably as the base class WigFile,
 * and the correct format (Wig/BigWig) can be autodetected by calling WigFile.autodetect()
 * 
 * @author timpalpant
 *
 */
public class TextWigFileReader extends WigFileReader {
	private static final long serialVersionUID = 7L;
	public static final String INDEX_EXTENSION = ".idx";
	public static final int KEY_GRANULARITY = 10_000;
	
	private static Logger log = Logger.getLogger(TextWigFileReader.class);
	
	private BufferedRandomAccessFile raf;
	private Path index;
	private Map<String,List<ContigIndex>> contigs = new HashMap<>();
	private long checksum;
	private SummaryStatistics stats;

	/**
	 * @param p the Path to the Wig file
	 * @throws IOException if an error occurs while opening or reading from the Wig file
	 * @throws WigFileException if an error occurs while indexing the Wig file
	 */
	public TextWigFileReader(Path p) throws IOException, WigFileFormatException {
		super(p);
		log.debug("Opening ASCII-text Wig file "+p);
		raf = new BufferedRandomAccessFile(p.toFile(), "r");

		String headerLine = raf.readLine2();
		if (headerLine.startsWith("track")) {
			try {
				header = TrackHeader.parse(headerLine);
			} catch (TrackHeaderException e) {
				log.error("Error parsing UCSC track header in file: " + p);
				e.printStackTrace();
			}
		}

		// Compute the checksum of this file
		checksum = ChecksumUtils.crc32(p);
		
		// Attempt to load an index from disk, or generate one otherwise
		index = p.resolveSibling(p.getFileName()+INDEX_EXTENSION);
		try {
			loadIndex(index, true);
		} catch (IOException | WigFileException e) {
			// (Re)generate if the index could not be loaded
			Files.deleteIfExists(index);
			generateIndex();
			saveIndex(index);
		}
	}
	
	/**
	 * Specify an index and forego matching checksum
	 * @param p the path to the Wig file
	 * @param index the path for the Wig file's precomputed index
	 * @throws IOException if an error occurs opening the Wig file or its index
	 * @throws WigFileException if an error occurs while loading the index
	 */
	public TextWigFileReader(Path p, Path index) throws IOException, WigFileException {
		super(p);
		log.debug("Opening ASCII-text Wig file "+p+" with index "+index);
		raf = new BufferedRandomAccessFile(p.toFile(), "r");
		this.index = index;

		String headerLine = raf.readLine2();
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
	
	/**
	 * Copy constructor - open a new file handle, but leave the index intact
	 * @param other
	 * @throws IOException
	 */
	public TextWigFileReader(TextWigFileReader other) throws IOException {
		super(other.p);
		index = other.index;
		log.debug("Opening new file handle to ASCII-text Wig file "+p);
		raf = new BufferedRandomAccessFile(other.p.toFile(), "r");
		
		// Shallow-copy the index
		contigs = other.contigs;
		checksum = other.checksum;
		stats = other.stats;
	}
	
	@Override
	public void close() {
		log.debug("Closing Wig file reader "+p);
		try {
			raf.close();
		} catch (IOException e) { 
			throw new RuntimeException("Error closing TextWigFile");
		}
	}
	
	@Override
	public Contig query(Interval interval) throws IOException, WigFileException {
		if (!includes(interval)) {
			throw new WigFileException("WigFile does not contain data for region: "+interval);
		}
		
		float[] values = new float[interval.length()];
		Arrays.fill(values, Float.NaN);
		// Load the values from each relevant contig into the array
		for (ContigIndex c : getContigsOverlappingInterval(interval)) {
			c.fill(raf, interval, values);
		}
		
		if (interval.isCrick()) {
			ArrayUtils.reverse(values);
		}
		
		return new Contig(interval, values);
	}
	
	@Override
	public SummaryStatistics queryStats(Interval interval) throws IOException, WigFileException {
		if (!includes(interval)) {
			throw new WigFileException("WigFile does not contain data for region: "+interval);
		}
		
		SummaryStatistics stats = new SummaryStatistics();
		// Load the values from each relevant contig
		for (ContigIndex c : getContigsOverlappingInterval(interval)) {
			c.fillStats(raf, interval, stats);
		}
		
		return stats;
	}
	
	private List<ContigIndex> getContigsOverlappingInterval(Interval interval) {
		List<ContigIndex> relevantContigs = new ArrayList<>();
		
		for (ContigIndex c : contigs.get(interval.getChr())) {
			if (c.getStop() >= interval.low() && c.getStart() <= interval.high()) {
				relevantContigs.add(c);
			}
		}
		
		log.debug("Found "+relevantContigs.size()+" contigs overlapping query interval "+interval);
		return relevantContigs;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("ASCII Text Wiggle file: " + header.toString() + "\n");
		
		s.append("Chromosomes:\n");
		for (String chr : chromosomes()) {
			s.append('\t').append(chr).append(" start=").append(getChrStart(chr)).append(" stop=").append(getChrStop(chr)).append('\n');
		}
		
		s.append("Contigs:\n");
		for (List<ContigIndex> chromContigs : contigs.values()) {
			for (ContigIndex c : chromContigs) {
				s.append("\t").append(c.toOutput()).append('\n');
			}
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
		return contigs.keySet();
	}
	
	@Override
	public int getChrStart(String chr) {
		if (!includes(chr)) {
			return -1;
		}
		
		int start = Integer.MAX_VALUE;
		for (ContigIndex c : contigs.get(chr)) {
			if (c.getStart() < start) {
				start = c.getStart();
			}
		}
		
		return start;
	}
	
	@Override
	public int getChrStop(String chr) {		
		if (!includes(chr)) {
			return -1;
		}
		
		int stop = -1;
		for (ContigIndex c : contigs.get(chr)) {
			if (c.getStop() > stop) {
				stop = c.getStop();
			}
		}
		
		return stop;
	}
	
	@Override
	public int getChrStep(String chr) {
		if (!includes(chr)) {
			return -1;
		}
		
		int step = Integer.MAX_VALUE;
		for (ContigIndex c : contigs.get(chr)) {
			if (c.isVariableStep()) {
				return 1;
			} else if (((FixedStepContigIndex)c).getStep() < step) {
				step = ((FixedStepContigIndex)c).getStep();
			}
		}
		
		return step;
	}

	@Override
	public int getChrSpan(String chr) {
		if (!includes(chr)) {
			return -1;
		}
		
		int span = Integer.MAX_VALUE;
		for (ContigIndex c : contigs.get(chr)) {
			if (c.getSpan() < span) {
				span = c.getSpan();
			}
		}
		
		return span;
	}
	
	@Override
	public boolean includes(String chr, int start, int stop) {
		return includes(chr) && getChrStart(chr) <= start && getChrStop(chr) >= stop;
	}
	
	@Override
	public boolean includes(String chr) {
		return contigs.containsKey(chr);
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
	private void generateIndex() throws IOException, WigFileFormatException {
		log.debug("Indexing ASCII text Wig file: " + p);
		
		// Skip the track line, if there is one
		raf.seek(0);
		long lineNum = 0;
		String line = raf.readLine2();
		if (!line.startsWith("track")) {
			raf.seek(0);
		} else {
			lineNum++;
		}

		// Index the Contigs and data in the Wig File by going through it once
		stats = new SummaryStatistics();
		contigs = new HashMap<>();
		ContigIndex contig = null;
		int bp = 0;
		double value;
		long cursor = raf.getFilePointer();
		int count = 0;
		while ((line = raf.readLine2()) != null) {
			lineNum++;
			
			if (line.startsWith(Contig.Type.FIXEDSTEP.getId()) || line.startsWith(Contig.Type.VARIABLESTEP.getId())) {
				// If this is the end of a previous Contig, store the stop info
				if (contigs.size() > 0) {
					contig.setStopLine(lineNum-1);
					contig.setStop(bp + contig.getSpan() - 1);
				}
				
				// Now parse the new Contig and add to the list of Contigs
				contig = ContigIndex.parseHeader(line);
				log.debug("Found contig header: "+line+" (line "+lineNum+")");
				if (!contigs.containsKey(contig.getChr())) {
					contigs.put(contig.getChr(), new ArrayList<ContigIndex>());
				}
				contigs.get(contig.getChr()).add(contig);
				
				// Set the new Contig's start info
				contig.setStartLine(lineNum+1);
				if (contig.isVariableStep()) {
					cursor = raf.getFilePointer();
					String firstLine = raf.readLine2();
					int delim = firstLine.indexOf('\t');
					if (delim == -1) {
						throw new WigFileFormatException("Illegal format in variableStep contig, line " + lineNum);
					}
					try {
						bp = Integer.parseInt(firstLine.substring(0, delim));
					} catch (NumberFormatException e) {
						throw new WigFileFormatException("Illegal format in variableStep contig, line " + lineNum);
					}
					contig.setStart(bp);
					raf.seek(cursor);
				} else {
					bp = contig.getStart() - ((FixedStepContigIndex)contig).getStep();
				}
			} else {
				if (contig == null) {
					throw new WigFileFormatException("Missing contig header (fixedStep or variableStep). Illegal format in Wig file, line " + lineNum);
				} else if (contig.isFixedStep()) {
					bp += ((FixedStepContigIndex)contig).getStep();
					try {
						value = Double.parseDouble(line);
					} catch (NumberFormatException e) {
						throw new WigFileFormatException("Illegal format in fixedStep contig, line " + lineNum);
					}
				} else {
					int delim = line.indexOf('\t');
					if (delim == -1) {
						throw new WigFileFormatException("Illegal format in variableStep contig, line " + lineNum);
					}
					
					try {
						bp = Integer.parseInt(line.substring(0, delim));
						value = Double.parseDouble(line.substring(delim+1));
					} catch (NumberFormatException e) {
						throw new WigFileFormatException("Illegal format in variableStep contig, line " + lineNum);
					}
				}
				
				if (!Double.isNaN(value) && !Double.isInfinite(value)) {
					count++;
					for (int i = 0; i < contig.getSpan(); i++) {
						stats.addValue(value);
					}
				}
				
				// Store this line in the index
				if ((lineNum-contig.getStartLine()) % KEY_GRANULARITY == 0) {
					contig.storeIndex(bp, cursor);
				}
			}
			
			// Store the cursor position if the next line will be stored in the index
			if ((lineNum+1-contig.getStartLine()) % KEY_GRANULARITY == 0) {
				cursor = raf.getFilePointer();
			}
		}
		
		// Set the stop info for the last contig
		if (contig != null) {
			contig.setStopLine(lineNum);
			contig.setStop(bp + contig.getSpan() - 1);
		}
		
		log.debug("Indexed "+count+" entries in Wig file");
	}
	
	/**
	 * Load information about this Wig file from a saved index
	 * @param p
	 * @throws IOException 
	 * @throws WigFileException 
	 */
	private void loadIndex(Path p, boolean matchChecksum) throws IOException, WigFileException {
		log.debug("Attempting to load Wig file index from disk");
		try (ObjectInputStream dis = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(p)))) {
			// Load and match version
			long version = dis.readLong();
			if (version != serialVersionUID) {
				log.warn("Version of index does not match version of Wig file!");
				throw new WigFileException("Cannot load index from older version!");
			}
			// Load and optionally match checksum
			long indexChecksum = dis.readLong();
			if (matchChecksum && indexChecksum != checksum) {
				log.warn("Index does not match checksum of Wig file!");
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
				// Load Contigs
				int numContigs = dis.readInt();
				contigs = new HashMap<String,List<ContigIndex>>();
				for (int i = 0; i < numContigs; i++) {
					ContigIndex contig = (ContigIndex) dis.readObject();
					if (!contigs.containsKey(contig.getChr())) {
						contigs.put(contig.getChr(), new ArrayList<ContigIndex>());
					}
					contigs.get(contig.getChr()).add(contig);
				}
				log.debug("Loaded index information for "+contigs.size()+" contigs");
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
		try (ObjectOutputStream dos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(p)))) {
			// Write the serialization version and corresponding Wig file checksum
			// at the top so it can easily be matched
			dos.writeLong(serialVersionUID);
			dos.writeLong(checksum);
			
			// Write statistics
			dos.writeObject(stats);
					
			// Write Contigs
			int numContigs = 0;
			for (List<ContigIndex> chromContigs : contigs.values()) {
				numContigs += chromContigs.size();
			}
			dos.writeInt(numContigs);
			for (List<ContigIndex> chromContigs : contigs.values()) {
				for (ContigIndex c : chromContigs) {
					dos.writeObject(c);
				}
			}
		} catch (IOException e) {
			log.error("Error saving Wig index information to disk!: " + e.getMessage());
			e.printStackTrace();
			// Remove the file because it's probably corrupt
			Files.deleteIfExists(p);
		}
	}

	@Override
	public TextWigFileReader clone() {
		try {
			return new TextWigFileReader(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}

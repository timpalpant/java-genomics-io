package edu.unc.genomics.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ed.javatools.BufferedRandomAccessFile;
import edu.ucsc.genome.TrackHeader;
import edu.ucsc.genome.TrackHeaderException;
import edu.unc.genomics.Interval;
import edu.unc.genomics.util.ChecksumUtils;

public class TextWigFile extends WigFile {
	private static final long serialVersionUID = -1092879147867842796L;
	public static final String INDEX_EXTENSION = ".wIdx";
	public static final int KEY_GRANULARITY = 10_000;
	
	private BufferedRandomAccessFile raf;
	private TrackHeader header;
	
	private List<Contig> contigs;
	private Set<String> chromosomes;
	
	private long checksum;
	
	private long numBases = 0;
	private double total = 0;
	private double mean = Double.NaN;
	private double stdev = Double.NaN;
	private double min = Double.MAX_VALUE;
	private double max = Double.MIN_VALUE;

	public TextWigFile(Path p) throws IOException, WigFileException {
		super(p);
		
		header = new TrackHeader("wiggle_0");
		contigs = new ArrayList<Contig>();
		chromosomes = new HashSet<String>();
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

		// Compute the checksum of this file
		checksum = ChecksumUtils.adler32(p);
		
		// Attempt to load an index from disk, or generate one otherwise
		Path indexFile = p.resolveSibling(p.getFileName()+INDEX_EXTENSION);
		try {
			loadIndex(indexFile);
		} catch (Exception e) {
			// (Re)generate if the index could not be loaded
			generateIndex();
			saveIndex(indexFile);
		}
	}

	@Override
	public float[] query(String chr, int start, int stop) throws IOException, WigFileException {
		if (!includes(chr, start, stop)) {
			throw new WigFileException("WigFile does not contain data for region: " + chr + ":" + start + "-" + stop);
		}
		
		int length = stop - start + 1;
		float[] result = new float[length];
		
		//BufferedReader reader = new BufferedReader(new FileReader(raf.getFD()));
		//int closestUpstreamBP = contigs.
		// Get the data into result
		raf.seek(10000);
		
		return result;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("ASCII Text Wiggle file: " + header.toString() + "\n");
		
		for (Contig c : contigs) {
			s.append("\t").append(c.toString()).append('\n');
		}
		
		s.append("Basic Statistics:\n");
		s.append("\tMean:\t\t\t").append(mean).append("\n");
		s.append("\tStandard Deviation:\t").append(stdev).append("\n");
		s.append("\tTotal:\t\t\t").append(total).append("\n");
		s.append("\tBases Covered:\t\t").append(numBases).append("\n");
		s.append("\tMin value:\t\t\t").append(min).append("\n");
		s.append("\tMax value:\t\t\t").append(max);
		
		return s.toString();
	}
	
	@Override
	public Set<String> chromosomes() {
		return chromosomes;
	}
	
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
	
	public int getChrStop(String chr) {		
		int stop = -1;
		for (Contig c : contigs) {
			if (c.getChr().equals(chr) && c.getStop() > stop) {
				stop = c.getStop();
			}
		}
		
		return stop;
	}
	
	public boolean includes(String chr, int start, int stop) {
		return includes(chr) && getChrStart(chr) <= start && getChrStop(chr) >= stop;
	}
	
	public boolean includes(String chr) {
		return chromosomes.contains(chr);
	}
	
	/**
	 * @return the numBases
	 */
	@Override
	public long numBases() {
		return numBases;
	}
	
	/**
	 * @return the total
	 */
	@Override
	public double total() {
		return total;
	}
	
	/**
	 * @return the mean
	 */
	@Override
	public double mean() {
		return mean;
	}
	
	/**
	 * @return the stdev
	 */
	@Override
	public double stdev() {
		return stdev;
	}
	
	/**
	 * @return the min
	 */
	@Override
	public double min() {
		return min;
	}
	
	/**
	 * @return the max
	 */
	@Override
	public double max() {
		return max;
	}
	
	/**
	 * Index this WigFile
	 * @throws IOException 
	 * @throws WigFileException 
	 */
	private void generateIndex() throws IOException, WigFileException {
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
		contigs = new ArrayList<Contig>();
		Contig contig = null;
		int bp = 0;
		double value;
		double sumOfSquares = 0;
		long cursor = raf.getFilePointer();
		while ((line = raf.readLine()) != null) {
			lineNum++;
			
			// Store the current position for putting in the index
			if (contigs.size() > 0 && (lineNum-contig.getStartLine()) % KEY_GRANULARITY == 0) {
				cursor = raf.getFilePointer();
			}
			
			if (line.startsWith(Contig.FIXED_STEP) || line.startsWith(Contig.VARIABLE_STEP)) {
				// If this is the end of a previous Contig, store the stop info
				if (contigs.size() > 0) {
					contig.setStopLine(lineNum-1);
					contig.setStopPos(raf.getFilePointer());
					contig.setStop(bp + contig.getSpan() - 1);
				}
				
				// Now parse the new Contig
				contig = Contig.parse(line);
				contigs.add(contig);
				
				// Set the new Contig's start info
				cursor = raf.getFilePointer();
				contig.setStartLine(lineNum);
				contig.setStartPos(cursor);
				if (contig instanceof VariableStepContig) {
					String firstLine = raf.readLine();
					int delim = firstLine.indexOf('\t');
					if (delim == -1) {
						throw new WigFileException("Illegal format in variableStep contig, line " + lineNum);
					}
					contig.setStart(Integer.parseInt(firstLine.substring(0, delim)));
				} else {
					bp = contig.getStart() - ((FixedStepContig)contig).getStep();
				}
			} else {
				if (contig instanceof FixedStepContig) {
					bp += ((FixedStepContig)contig).getStep();
					value = Double.parseDouble(line);
				} else {
					int delim = line.indexOf('\t');
					if (delim == -1) {
						throw new WigFileException("Illegal format in variableStep contig, line " + lineNum);
					}
					
					bp = Integer.parseInt(line.substring(0, delim));
					value = Double.parseDouble(line.substring(delim+1));
				}
				
				if (value < min) {
					min = value;
				}
				
				if (value > max) {
					max = value;
				}
				
				numBases += contig.getSpan();
				total += contig.getSpan() * value;
				sumOfSquares += contig.getSpan() * value * value;
				
				// Store this line in the index
				if ((lineNum - contig.getStartLine()) % KEY_GRANULARITY == 0) {
					contig.storeIndex(bp, cursor);
				}
			}
		}
		
		mean = total / numBases;
		double variance = (sumOfSquares - total*mean) / numBases;
		stdev = Math.sqrt(variance);

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
	private void loadIndex(Path p) throws IOException, WigFileException {
		InputStream is = Files.newInputStream(p);
		BufferedInputStream bis = new BufferedInputStream(is);
		ObjectInputStream dis = new ObjectInputStream(bis);
		
		// Load and match version and checksum
		long version = dis.readLong();
		long indexChecksum = dis.readLong();
		if (version != serialVersionUID || indexChecksum != checksum) {
			throw new WigFileException("Attempting to load index with invalid version or checksum!");
		}
		
		// Load statistics
		numBases = dis.readLong();
		total = dis.readDouble();
		mean = dis.readDouble();
		stdev = dis.readDouble();
		min = dis.readDouble();
		max = dis.readDouble();
		
		try {
			// Load chromosomes
			int numChromosomes = dis.readInt();
			for (int i = 0; i < numChromosomes; i++) {
				String chr = (String) dis.readObject();
				chromosomes.add(chr);
			}
			
			// Load Contigs
			int numContigs = dis.readInt();
			for (int i = 0; i < numContigs; i++) {
				Contig contig = (Contig) dis.readObject();
				contigs.add(contig);
			}
		} catch (ClassNotFoundException e) {
			System.err.println("ClassNotFoundException while loading Wig index from file");
			e.printStackTrace();
			throw new WigFileException("ClassNotFoundException while trying to load Wig index from file");
		}
	}
	
	/**
	 * Save the statistics and data index about this Wig file to disk
	 * @param p
	 * @throws IOException 
	 */
	private void saveIndex(Path p) throws IOException {
		OutputStream os = Files.newOutputStream(p);
		BufferedOutputStream bos = new BufferedOutputStream(os);
		ObjectOutputStream dos = new ObjectOutputStream(bos);
		
		// Write the serialization version and corresponding Wig file checksum
		// at the top so it can easily be matched
		dos.writeLong(serialVersionUID);
		dos.writeLong(checksum);
		
		// Write statistics
		dos.writeLong(numBases);
		dos.writeDouble(total);
		dos.writeDouble(mean);
		dos.writeDouble(stdev);
		dos.writeDouble(min);
		dos.writeDouble(max);	
		
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
	}

	/**
	 * @author timpalpant
	 * Holds information about a Contig in a WigFile
	 */
	private static class Contig extends Interval implements Serializable {
		private static final long serialVersionUID = 7665673936467048945L;
		
		private int span;
		private long startLine;
		private long stopLine;
		private long startPos;
		private long stopPos;
		private Map<Integer, Long> index;
		
		private static final String FIXED_STEP = "fixedStep";
		private static final String VARIABLE_STEP = "variableStep";
		
		public Contig(String chr, int start, int stop, int span) {
			super(chr, start, stop);
			this.span = span;
			this.index = new HashMap<Integer, Long>();
		}
		
		/**
		 * Parse a contig header line from a Wig file
		 * @param headerLine
		 * @return
		 * @throws WigFileException 
		 */
		public static Contig parse(String headerLine) throws WigFileException {
			if (headerLine.startsWith(FIXED_STEP)) {
				return FixedStepContig.parse(headerLine);
			} else if (headerLine.startsWith(VARIABLE_STEP)) {
				return VariableStepContig.parse(headerLine);
			} else {
				throw new WigFileException("Unknown Contig type: " + headerLine);
			}
		}
		
		public void storeIndex(int bp, long pos) {
			index.put(bp, pos);
		}
		
		/**
		 * @param bp
		 * @return the closest known upstream position in the index
		 */
		public long getClosestUpstreamIndexedPos(int bp) {
			int closestBP = -1;
			long closestPos = -1;
			for (Map.Entry<Integer, Long> entry : index.entrySet()) {
		    if (entry.getKey() > closestBP && entry.getKey() <= bp) {
		    	closestBP = entry.getKey();
		    	closestPos = entry.getValue();
		    }
			}
			
			return closestPos;
		}
		
		/**
		 * @return the span
		 */
		public int getSpan() {
			return span;
		}

		/**
		 * @return the startLine
		 */
		public long getStartLine() {
			return startLine;
		}

		/**
		 * @param startLine the startLine to set
		 */
		public void setStartLine(long startLine) {
			this.startLine = startLine;
		}

		/**
		 * @return the stopLine
		 */
		public long getStopLine() {
			return stopLine;
		}

		/**
		 * @param stopLine the stopLine to set
		 */
		public void setStopLine(long stopLine) {
			this.stopLine = stopLine;
		}

		/**
		 * @return the startPos
		 */
		public long getStartPos() {
			return startPos;
		}

		/**
		 * @param startPos the startPos to set
		 */
		public void setStartPos(long startPos) {
			this.startPos = startPos;
		}

		/**
		 * @return the stopPos
		 */
		public long getStopPos() {
			return stopPos;
		}

		/**
		 * @param stopPos the stopPos to set
		 */
		public void setStopPos(long stopPos) {
			this.stopPos = stopPos;
		}
	}
	
	private static class FixedStepContig extends Contig {
		private static final long serialVersionUID = -142695785731234833L;
		private int step;
		
		public FixedStepContig(String chr, int start, int stop, int span, int step) {
			super(chr, start, stop, span);
			this.step = step;
		}
		
		public static FixedStepContig parse(String headerLine) throws WigFileException {
			String[] tokens = headerLine.split(" ");
			if (tokens.length == 0 || !tokens[0].equals(Contig.FIXED_STEP)) {
				throw new WigFileException("Not a valid fixedStep header line: " + headerLine);
			}
			
			String chr = "";
			int start = 1;
			int span = 1;
			int step = 1;
			for (int i = 1; i < tokens.length; i++ ) {
				String s = tokens[i];
				String[] pair = s.split("=");
				if (pair.length != 2) {
					throw new WigFileException("Invalid keypair in fixedStep header line: " + s);
				}
				
				String key = pair[0];
				String value = pair[1];
				switch(key) {
				case "chrom":
					chr = value;
					break;
				case "start":
					start = Integer.parseInt(value);
					break;
				case "span":
					span = Integer.parseInt(value);
					break;
				case "step":
					step = Integer.parseInt(value);
					break;
				default:
					throw new WigFileException("Invalid attribute in fixedStep header line: " + key);
				}
			}
			
			return new FixedStepContig(chr, start, -1, span, step);
		}
		
		/**
		 * @param bp
		 * @return the line on which to find bp in this contig
		 * @throws WigFileException 
		 */
		public long getLineNumForBasePair(int bp) throws WigFileException {
			if (bp < getStart() || bp > getStop()) {
				throw new WigFileException("Specified base pair (" + bp + ") does not exist in this Contig");
			}
			
			return getStartLine() + (bp-getStop())/step;
		}
		
		public int getBasePairForLineNum(long lineNum) throws WigFileException {
			if (lineNum < getStartLine() || lineNum > getStopLine()) {
				throw new WigFileException("Line " + lineNum + " is not a part of this contig");
			}
			
			return (int)(getStart() + step*(lineNum - getStartLine()));
		}
		
		@Override
		public String toString() {
			return Contig.FIXED_STEP + " chrom=" + getChr() + " start=" + getStart() + " span=" + getSpan() + " step=" + step;
		}

		/**
		 * @return the step
		 */
		public int getStep() {
			return step;
		}
	}
	
	private static class VariableStepContig extends Contig {
		private static final long serialVersionUID = 3139905829545756903L;

		public VariableStepContig(String chr, int start, int stop, int span) {
			super(chr, start, stop, span);
		}
		
		public static VariableStepContig parse(String headerLine) throws WigFileException {
			String[] tokens = headerLine.split(" ");
			if (tokens.length == 0 || !tokens[0].equals(Contig.VARIABLE_STEP)) {
				throw new WigFileException("Not a valid variableStep header line: " + headerLine);
			}
			
			String chr = "";
			int start = 1;
			int span = 1;
			for (int i = 1; i < tokens.length; i++ ) {
				String s = tokens[i];
				String[] pair = s.split("=");
				if (pair.length != 2) {
					throw new WigFileException("Invalid keypair in variableStep header line: " + s);
				}
				
				String key = pair[0];
				String value = pair[1];
				switch(key) {
				case "chrom":
					chr = value;
					break;
				case "span":
					span = Integer.parseInt(value);
					break;
				default:
					throw new WigFileException("Invalid attribute in variableStep header line: " + key);
				}
			}
			
			return new VariableStepContig(chr, start, -1, span);
		}
		
		@Override
		public String toString() {
			return Contig.VARIABLE_STEP + " chrom=" + getChr() + " span=" + getSpan();
		}
	}
}

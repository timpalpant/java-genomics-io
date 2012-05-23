package edu.unc.genomics.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.broad.igv.bbfile.WigItem;

import edu.unc.genomics.Interval;

/**
 * Hold index information about a fixedStep contig in a TextWigFile
 * 
 * @author timpalpant
 *
 */
class FixedStepContig extends Contig {
	
	private static final Logger log = Logger.getLogger(FixedStepContigIterator.class);
	
	private static final long serialVersionUID = -142695785731234833L;
	
	private int step;
	
	public FixedStepContig(String chr, int start, int stop, int span, int step) {
		super(chr, start, stop, span);
		this.step = step;
	}
	
	public static FixedStepContig parseHeader(String headerLine) throws WigFileException {
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
		
		return getStartLine() + (bp-getStart())/step;
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

	@Override
	public FixedStepContigIterator query(RandomAccessFile raf, Interval interval) throws IOException, WigFileException {
		return new FixedStepContigIterator(raf, interval);
	}
	
	private class FixedStepContigIterator implements Iterator<WigItem> {

		private final RandomAccessFile raf;
		private final Interval interval;
		private final int low;
		private final int high;
		private final long startLine;
		private final long stopLine;
		private long currentLine;
		private int bp;
		private int itemIndex = 0;
		
		public FixedStepContigIterator(final RandomAccessFile raf, final Interval interval) throws IOException, WigFileException {
			this.raf = raf;
			this.interval = interval;
			
			// Clamp to bases that are covered by this Contig
			low = Math.max(start, interval.low());
			high = Math.min(stop, interval.high());
			
			// Figure out what lines we need
			startLine = getLineNumForBasePair(low);
			stopLine = getLineNumForBasePair(high);
			
			// Find the closest known upstream base-pair position and seek
			int closestUpstream = getUpstreamIndexedBP(low);
			raf.seek(getIndex(closestUpstream));
			
			// Skip to the start line
			for (currentLine = getLineNumForBasePair(closestUpstream); 
					currentLine < startLine; currentLine++) {
				raf.readLine();
			}
			
			// Set the base pair we are at (may be < start if span > 1)
			bp = getBasePairForLineNum(currentLine);
		}
		
		@Override
		public boolean hasNext() {
			return currentLine <= stopLine;
		}

		@Override
		public WigItem next() {
			try {
				String line = raf.readLine();
				currentLine++;
				
				float value = Float.parseFloat(line);
				WigItem item = new WigItem(itemIndex++, chr, bp, bp+getSpan()-1, value);
							
				bp += getStep();
				
				return item;
			} catch (IOException e) {
				throw new RuntimeException("Error getting next entry in Wig file!");
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Cannot remove elements from Wig file");
		}
	}
}
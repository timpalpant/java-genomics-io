package edu.unc.genomics.io;

import java.io.IOException;

import ed.javatools.BufferedRandomAccessFile;
import edu.unc.genomics.Contig;
import edu.unc.genomics.Interval;

/**
 * Hold index information about a fixedStep contig in a TextWigFile
 * 
 * @author timpalpant
 *
 */
class FixedStepContigIndex extends ContigIndex {
	
	private static final long serialVersionUID = -142695785731234833L;
	
	private int step;
	
	public FixedStepContigIndex(String chr, int start, int stop, int span, int step) {
		super(chr, start, stop, span);
		this.step = step;
	}
	
	public static FixedStepContigIndex parseHeader(String headerLine) throws WigFileFormatException {
		String[] tokens = headerLine.split(" ");
		if (tokens.length == 0 || !tokens[0].equals(Contig.Type.FIXEDSTEP.getId())) {
			throw new WigFileFormatException("Not a valid fixedStep header line: " + headerLine);
		}
		
		String chr = "";
		int start = 1;
		int span = 1;
		int step = 1;
		for (int i = 1; i < tokens.length; i++ ) {
			String s = tokens[i];
			String[] pair = s.split("=");
			if (pair.length != 2) {
				throw new WigFileFormatException("Invalid keypair in fixedStep header line: " + s);
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
				throw new WigFileFormatException("Invalid attribute in fixedStep header line: " + key);
			}
		}
		
		return new FixedStepContigIndex(chr, start, -1, span, step);
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
	public String toOutput() {
		return Contig.Type.FIXEDSTEP.getId() + " chrom=" + getChr() + " start=" + getStart() + " span=" + getSpan() + " step=" + step;
	}

	/**
	 * @return the step
	 */
	public int getStep() {
		return step;
	}
	
	@Override
	public boolean isFixedStep() {
		return true;
	}

	@Override
	public boolean isVariableStep() {
		return false;
	}

	@Override
	public void fill(BufferedRandomAccessFile raf, Interval interval, float[] values) throws WigFileException, IOException {
		// Clamp to bases that are covered by this Contig
		int low = Math.max(start, interval.low());
		int high = Math.min(stop, interval.high());
		
		// Figure out what lines we need
		long startLine = getLineNumForBasePair(low);
		long stopLine = getLineNumForBasePair(high);
		
		// Find the closest known upstream base-pair position in the index
		int closestUpstream = getUpstreamIndexedBP(low);
		synchronized (raf) {
			// Seek to the closest known position in the index
			raf.seek(getIndex(closestUpstream));
			
			// Skip to the start line
			long currentLine;
			for (currentLine = getLineNumForBasePair(closestUpstream); currentLine < startLine; currentLine++) {
				raf.readLine2();
			}
			
			// Get the base pair we are at (may be < start if span > 1)
			int bp = getBasePairForLineNum(currentLine);
			
			// Load the values from disk into the array
			while (currentLine <= stopLine) {
				String line = raf.readLine2();
				currentLine++;
				
				float value = Float.parseFloat(line);
				if (!Float.isNaN(value)) {
					for (int i = bp; i <= bp+getSpan()-1; i++) {
						if (interval.includes(i)) {
							values[i-interval.low()] = value;
						}
					}
				}
							
				bp += getStep();
			}
		}
	}
}
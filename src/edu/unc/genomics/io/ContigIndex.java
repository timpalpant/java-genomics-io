package edu.unc.genomics.io;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ed.javatools.BufferedRandomAccessFile;
import edu.unc.genomics.Contig;
import edu.unc.genomics.Interval;
import edu.unc.genomics.WigEntry;

/**
 * Holds index information about a Contig in a WigFile
 * 
 * @author timpalpant
 * 
 */
abstract class ContigIndex extends Interval implements Serializable {
	
	private static final long serialVersionUID = 7665673936467048945L;
	
	private int span;
	private long startLine;
	private long stopLine;
	private Map<Integer, Long> index = new HashMap<Integer, Long>();
	
	protected ContigIndex(String chr, int start, int stop, int span) {
		super(chr, start, stop);
		this.span = span;
	}
	
	/**
	 * Parse a contig header line from a Wig file
	 * @param headerLine
	 * @return
	 * @throws WigFileException 
	 */
	public static ContigIndex parseHeader(String headerLine) throws WigFileFormatException {
		if (headerLine.startsWith(Contig.Type.FIXEDSTEP.getId())) {
			return FixedStepContigIndex.parseHeader(headerLine);
		} else if (headerLine.startsWith(Contig.Type.VARIABLESTEP.getId())) {
			return VariableStepContigIndex.parseHeader(headerLine);
		} else {
			throw new WigFileFormatException("Unknown Contig type: " + headerLine);
		}
	}
	
	public abstract Iterator<WigEntry> query(BufferedRandomAccessFile raf, Interval interval) throws IOException, WigFileException;
	
	public abstract boolean isFixedStep();
	
	public abstract boolean isVariableStep();
	
	@Override
	public abstract String toOutput();
	
	public void storeIndex(int bp, long pos) {
		index.put(bp, pos);
	}
	
	public long getIndex(int bp) {
		return index.get(bp);
	}
	
	/**
	 * @param bp
	 * @return the closest known upstream bp in the index
	 */
	public int getUpstreamIndexedBP(int bp) {
		int closestBP = -1;
		// TODO Better way to seek for indexed position (R-tree)
		for (int indexBP : index.keySet()) {
			if (indexBP > closestBP && indexBP <= bp) {
	    	closestBP = indexBP;
	    }
		}
		
		return closestBP;
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
}
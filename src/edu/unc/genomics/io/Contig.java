package edu.unc.genomics.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.broad.igv.bbfile.WigItem;

import edu.unc.genomics.Interval;

/**
 * @author timpalpant
 * Holds information about a Contig in a WigFile
 */
abstract class Contig extends Interval implements Serializable {
	private static final long serialVersionUID = 7665673936467048945L;
	
	private int span;
	private long startLine;
	private long stopLine;
	private Map<Integer, Long> index = new HashMap<Integer, Long>();
	
	protected static final String FIXED_STEP = "fixedStep";
	protected static final String VARIABLE_STEP = "variableStep";
	
	protected Contig(String chr, int start, int stop, int span) {
		super(chr, start, stop);
		this.span = span;
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
	
	public abstract Iterator<WigItem> query(RandomAccessFile raf, String chr, 
			int start, int stop) throws IOException, WigFileException;
	
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
		// TODO: Better way to seek for indexed position (R-tree)
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
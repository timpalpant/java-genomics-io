package edu.unc.genomics;

import edu.unc.genomics.io.IntervalFileFormatException;

/**
 * @author timpalpant
 *
 */
public class BedGraphEntry extends ValuedInterval {

	private static final long serialVersionUID = 3205776570270297617L;

	/**
	 * @param chr
	 * @param start
	 * @param stop
	 * @param id
	 * @param value
	 */
	public BedGraphEntry(String chr, int start, int stop, String id, Double value) {
		super(chr, start, stop, id, value);
	}

	/**
	 * @param chr
	 * @param start
	 * @param stop
	 * @param id
	 */
	public BedGraphEntry(String chr, int start, int stop, String id) {
		super(chr, start, stop, id);
	}

	/**
	 * @param chr
	 * @param start
	 * @param stop
	 */
	public BedGraphEntry(String chr, int start, int stop) {
		super(chr, start, stop);
	}
	
	public static BedGraphEntry parse(final String line) {
		if (line.startsWith("#") || line.startsWith("track")) {
			return null;
		}
		
		String[] entry = line.split("\t");
		if (entry.length < 3) {
			throw new IntervalFileFormatException("Invalid BedGraph entry has < 3 columns");
		}
		
		String chr = entry[0];
		// BedGraph is 0-indexed, half-open
		int start = Integer.parseInt(entry[1]) + 1;
		int stop = Integer.parseInt(entry[2]);
		if (start > stop) {
			throw new IntervalFileFormatException("Invalid BedGraph entry has start > stop");
		}
		BedGraphEntry bedGraph = new BedGraphEntry(chr, start, stop);
		
		if (entry.length >= 4) {
			bedGraph.setValue(Double.parseDouble(entry[3]));
		}
		
		return bedGraph;
	}

}

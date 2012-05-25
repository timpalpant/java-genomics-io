package edu.unc.genomics;

import org.broad.igv.bbfile.WigItem;

/**
 * Holds an entry in a Wig file
 * @author timpalpant
 *
 */
public class WigEntry extends ValuedInterval {

	private static final long serialVersionUID = 904687413313801266L;

	public WigEntry(String chr, int start, int stop, float value) {
		super(chr, start, stop, null, value);
	}
	
	/**
	 * Construct a new WigEntry from a BigWig WigItem
	 * Change the coordinates from 0-based, half-open to 1-based, closed
	 * @param item a WigItem from a BigWig reader
	 */
	public WigEntry(WigItem item) {
		this(item.getChromosome(), item.getStartBase()+1, item.getEndBase(), item.getWigValue());
	}

}

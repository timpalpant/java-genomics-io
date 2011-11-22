/**
 * 
 */
package edu.unc.genomics;

import edu.unc.genomics.io.IntervalFileFormatException;
import edu.unc.genomics.util.ArrayUtils;

/**
 * @author timpalpant
 *
 */
public class BedEntry extends ValuedInterval {
	private static final long serialVersionUID = 8657984166944604756L;
	
	private String itemRgb;
	private int thickStart;
	private int thickEnd;
	private int blockCount;
	private int[] blockSizes;
	private int[] blockStarts;

	/**
	 * @param chr
	 * @param start
	 * @param stop
	 */
	public BedEntry(String chr, int start, int stop) {
		super(chr, start, stop);
	}
	
	public static BedEntry parse(String line) {
		String[] entry = line.split("\t");
		if (entry.length < 3) {
			throw new IntervalFileFormatException("Invalid Bed entry has < 3 columns");
		}
		
		String chr = entry[0];
		int start = Integer.parseInt(entry[1]) + 1; // Bed is 0-indexed
		int stop = Integer.parseInt(entry[2]); // and half-open
		BedEntry bed = new BedEntry(chr, start, stop);
		
		if (entry.length >= 4) {
			bed.setId(entry[3]);
		}
		
		if (entry.length >= 5 && !entry[4].equalsIgnoreCase(".")) {
			bed.setValue(Double.valueOf(entry[4]));
		}
		
		// Reverse start/stop if on the - strand
		if (entry.length >= 6 && entry[5].equalsIgnoreCase("-") && start < stop) {
			int tmp = bed.getStart();
			bed.setStart(stop);
			bed.setStop(tmp);
		}
		
		if (entry.length >= 8) {
			bed.setThickStart(Integer.parseInt(entry[6])+1);
			bed.setThickEnd(Integer.parseInt(entry[7]));
		}
		
		if (entry.length >= 9) {
			bed.setItemRgb(entry[8]);
		}
		
		if (entry.length >= 12) {
			bed.setBlockCount(Integer.parseInt(entry[9]));
			bed.setBlockSizes(ArrayUtils.mapToInt(entry[10].split(",")));
			bed.setBlockStarts(ArrayUtils.mapToInt(entry[11].split(",")));
		}
		
		return bed;
	}
	
	public String toBed6() {
		StringBuilder s = new StringBuilder();
		
		s.append(chr).append('\t');
		s.append(low()-1).append('\t');
		s.append(high()).append('\t');
		if (id != null) {
			s.append(id);
		} else {
			s.append('.');
		}
		s.append('\t');
		if (value != null) {
			s.append(value);
		} else {
			s.append('.');
		}
		s.append('\t');
		s.append(strand());
		
		return s.toString();
	}
	
	public String toBed12() {
		StringBuilder s = new StringBuilder(toBed6());
		
		// TODO: Add Bed12 fields
		
		return s.toString();
	}
	
	@Override
	public String toBed() {
		return toBed6();
	}

	/**
	 * @return the itemRgb
	 */
	public String getItemRgb() {
		return itemRgb;
	}

	/**
	 * @param itemRgb the itemRgb to set
	 */
	public void setItemRgb(String itemRgb) {
		this.itemRgb = itemRgb;
	}

	/**
	 * @return the thickStart
	 */
	public int getThickStart() {
		return thickStart;
	}

	/**
	 * @param thickStart the thickStart to set
	 */
	public void setThickStart(int thickStart) {
		this.thickStart = thickStart;
	}

	/**
	 * @return the thickEnd
	 */
	public int getThickEnd() {
		return thickEnd;
	}

	/**
	 * @param thickEnd the thickEnd to set
	 */
	public void setThickEnd(int thickEnd) {
		this.thickEnd = thickEnd;
	}

	/**
	 * @return the blockCount
	 */
	public int getBlockCount() {
		return blockCount;
	}

	/**
	 * @param blockCount the blockCount to set
	 */
	public void setBlockCount(int blockCount) {
		this.blockCount = blockCount;
	}

	/**
	 * @return the blockSizes
	 */
	public int[] getBlockSizes() {
		return blockSizes;
	}

	/**
	 * @param blockSizes the blockSizes to set
	 */
	public void setBlockSizes(int[] blockSizes) {
		this.blockSizes = blockSizes;
	}

	/**
	 * @return the blockStarts
	 */
	public int[] getBlockStarts() {
		return blockStarts;
	}
	
	/**
	 * @param blockStarts the blockStarts to set
	 */
	public void setBlockStarts(int[] blockStarts) {
		this.blockStarts = blockStarts;
	}

}

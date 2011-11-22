package edu.unc.genomics;

import net.sf.samtools.SAMRecord;

/**
 * @author timpalpant
 *
 */
public class SAMEntry extends Interval {

	private static final long serialVersionUID = -439658908814430105L;
	
	private SAMRecord r;
	
	/**
	 * @param chr
	 * @param start
	 * @param stop
	 */
	public SAMEntry(SAMRecord r) {
		super(r.getReferenceName(), r.getAlignmentStart(), r.getAlignmentEnd());
		this.r = r;
	}
	
	// TODO: Add accessors for other SAMRecord methods

}

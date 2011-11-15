package edu.unc.genomics;

public class Read extends Interval {
	private String seq;
	private String qual;
	
	/**
	 * @return the seq
	 */
	public String getSeq() {
		return seq;
	}
	
	/**
	 * @param seq the seq to set
	 */
	public void setSeq(String seq) {
		this.seq = seq;
	}
	
	/**
	 * @return the qual
	 */
	public String getQual() {
		return qual;
	}
	
	/**
	 * @param qual the qual to set
	 */
	public void setQual(String qual) {
		this.qual = qual;
	}
}

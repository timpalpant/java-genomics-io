package edu.unc.genomics;

public class FastqEntry extends Sequence {
	
	private String qual;

	public FastqEntry(String id, String seq, String qual) {
		super(id, seq);
		this.qual = qual;
	}

	public String getQual() {
		return qual;
	}

	public void setQual(String qual) {
		this.qual = qual;
	}

}

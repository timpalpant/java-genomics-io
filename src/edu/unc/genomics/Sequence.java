package edu.unc.genomics;

/**
 * Abstract class for sequence entries (FASTA, FASTQ, TwoBit)
 * @author timpalpant
 *
 */
public class Sequence {
	private String id;
	private String seq;
	
	public Sequence(String id, String seq) {
		this.id = id;
		this.seq = seq;
	}
	
	public Sequence(String seq) {
		this(null, seq);
	}

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}

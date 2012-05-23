package edu.unc.genomics;

/**
 * An enumeration to signify +/- (Watson/Crick) strand
 * A Watson interval is one in which stop >= start
 * A Crick interval is one in which start > stop
 * 
 * @author timpalpant
 *
 */
public enum Strand {
	WATSON("+"),
	CRICK("-");
	
	private final String id;
	
	Strand(final String id) {
		this.id = id;
	}
	
	public static Strand forId(final String id) {
		for (Strand s : Strand.values()) {
			if (s.getId().equals(id)) {
				return s;
			}
		}
		
		return null;
	}
	
	public String getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return getId();
	}
}

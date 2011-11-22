package edu.unc.genomics;

import java.io.Serializable;

public class Interval implements Serializable {
	
	private static final long serialVersionUID = 7515817773660876485L;
	
	protected String chr;
	protected int start;
	protected int stop;
	
	public Interval(String chr, int start, int stop) {
		this.chr = chr;
		this.start = start;
		this.stop = stop;
	}
	
	public String toBed() {
		return chr + "\t" + (low()-1) + "\t" + high() + "\t.\t.\t" + strand();
	}
	
	public String toBedGraph() {
		return chr + "\t" + (low()-1) + "\t" + high();
	}
	
	public String toGFF() {
		return chr + "\tSpotArray\tfeature\t" + low() + "\t" + high() + "\t.\t" + strand() + "\t.\tprobe_id=no_id;count=1";
	}
	
	public int center() {
		return (start + stop) / 2;
	}
	
	public int length() {
		return Math.abs(stop-start) + 1;
	}
	
	public boolean includes(final int bp) {
		return low() <= bp && high() >= bp;
	}
	
	public int low() {
		return Math.min(start, stop);
	}
	
	public int high() {
		return Math.max(start, stop);
	}

	public boolean isWatson() {
		return stop >= start;
	}
	
	public boolean isCrick() {
		return !isWatson();
	}
	
	public String strand() {
		return isWatson() ? "+" : "-";
	}
	
	public boolean isValid() {
		return start > 0 && stop > 0;
	}
	
  /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return chr + ":" + start + "-" + stop;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chr == null) ? 0 : chr.hashCode());
		result = prime * result + start;
		result = prime * result + stop;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Interval))
			return false;
		Interval other = (Interval) obj;
		if (chr == null) {
			if (other.chr != null)
				return false;
		} else if (!chr.equals(other.chr))
			return false;
		if (start != other.start)
			return false;
		if (stop != other.stop)
			return false;
		return true;
	}
	
	public String getChr() {
		return chr;
	}
	
	public void setChr(final String chr) {
		this.chr = chr;
	}
	
	public int getStart() {
		return start;
	}
	
	public void setStart(final int start) {
		this.start = start;
	}
	
	public int getStop() {
		return stop;
	}
	
	public void setStop(final int stop) {
		this.stop = stop;
	}
}

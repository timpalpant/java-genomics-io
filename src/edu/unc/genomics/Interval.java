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
	
	/**
	 * Parse an Interval from a UCSC-like string of the form "chrV:12-14"
	 * @param s an interval String to parse
	 * @return an Interval object parsed from s
	 * @throws IntervalException if s is not a valid interval string
	 */
	public static Interval parse(String s) throws IntervalException {
		int colon = s.indexOf(':');
		if (colon == -1) {
			throw new IntervalException("Cannot parse invalid interval string " + s);
		}
		
		int dash = s.indexOf('-');
		if (dash == -1) {
			throw new IntervalException("Cannot parse invalid interval string " + s);
		}
		
		try {
			String chr = s.substring(0, colon);
			int start = Integer.parseInt(s.substring(colon+1, dash).replaceAll(",", ""));
			int stop = Integer.parseInt(s.substring(dash+1).replaceAll(",", ""));
			return new Interval(chr, start, stop);
		} catch (NumberFormatException e) {
			throw new IntervalException("Cannot parse invalid interval string " + s);
		}
	}
	
	/**
	 * Return an Interval in Bed format
	 * @return an Interval in Bed format
	 */
	public String toBed() {
		return chr + "\t" + (low()-1) + "\t" + high() + "\t.\t.\t" + strand();
	}
	
	/**
	 * Return an Interval in BedGraph format
	 * @return an Interval in BedGraph format
	 */
	public String toBedGraph() {
		return chr + "\t" + (low()-1) + "\t" + high();
	}
	
	/**
	 * Return an Interval in GFF format
	 * @return an Interval in GFF format
	 */
	public String toGFF() {
		return chr + "\tSpotArray\tfeature\t" + low() + "\t" + high() + "\t.\t" + strand() + "\t.\tprobe_id=no_id;count=1";
	}
	
	/**
	 * The center of this interval, equal to (start+stop)/2
	 * If the interval does not have a perfect center (even length intervals)
	 * then the center is rounded down (floor)
	 * @return the center base pair of this interval
	 */
	public final int center() {
		return (start + stop) / 2;
	}
	
	/**
	 * The length of this interval, equal to high-low+1
	 * @return
	 */
	public int length() {
		return Math.abs(stop-start) + 1;
	}
	
	/**
	 * If this interval is on the specified chromosome and includes the base pair
	 * @param chr
	 * @param bp
	 * @return
	 */
	public boolean includes(final String chr, final int bp) {
		return this.chr == chr && includes(bp);
	}
	
	/**
	 * If this interval includes the given base pair (assumes that it is on the correct chromosome)
	 * @param bp
	 * @return
	 */
	public boolean includes(final int bp) {
		return low() <= bp && high() >= bp;
	}
	
	/**
	 * The lowest genomic coordinate, i.e min { start, stop }
	 * @return start or stop, whichever is lower
	 */
	public final int low() {
		return Math.min(start, stop);
	}
	
	/**
	 * The highest genomic coordinate, i.e max { start, stop }
	 * @return start or stop, whichever is higher
	 */
	public final int high() {
		return Math.max(start, stop);
	}

	/**
	 * If this interval is on the + strand, i.e. stop >= start
	 * @return true if this interval is on the + strand, false otherwise
	 */
	public final boolean isWatson() {
		return stop >= start;
	}
	
	/**
	 * If this interval is on the - strand, i.e. stop < start
	 * @return true if this interval is on the - strand, false otherwise
	 */
	public final boolean isCrick() {
		return !isWatson();
	}
	
	/**
	 * The strand of this Interval, either "+" or "-"
	 * @return "+" if this Interval is Watson, "-" if this Inteval is Crick
	 */
	public final Strand strand() {
		return isWatson() ? Strand.WATSON : Strand.CRICK;
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

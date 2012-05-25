package edu.unc.genomics;

/**
 * An Interval that can optionally have an ID and a numerical value associated with it
 * 
 * @author timpalpant
 *
 */
public class ValuedInterval extends Interval {
	private static final long serialVersionUID = 2976113135643438146L;
	
	protected String id;
	protected Number value;
	
	public ValuedInterval(String chr, int start, int stop, String id, Number value) {
		super(chr, start, stop);
		this.id = id;
		this.value = value;
	}
	
	/**
	 * Initialize a new ValuedInterval with default value (null)
	 * @param chr
	 * @param start
	 * @param stop
	 * @param id
	 */
	public ValuedInterval(String chr, int start, int stop, String id) {
		this(chr, start, stop, id, null);
	}
	
	/**
	 * Initialize a ValuedInterval with default value (null) and no id (null)
	 * @param chr
	 * @param start
	 * @param stop
	 */
	public ValuedInterval(String chr, int start, int stop) {
		this(chr, start, stop, null);
	}
	
	@Override
	public String toBed() {
		String idStr = (id == null) ? "." : id;
		String valueStr = (getValue() == null) ? "." : String.valueOf(getValue().intValue());
		return getChr() + "\t" + (low()-1) + "\t" + high() + "\t" + idStr + "\t" + valueStr + "\t" + strand();
	}
	
	@Override
	public String toBedGraph() {
		if (getValue() == null) {
			return super.toBedGraph();
		} else {
			return super.toBedGraph() + "\t" + getValue().toString();
		}
	}
	
	@Override
	public String toGFF() {
		String idStr = (id == null) ? "no_id" : id;
		String valueStr = (getValue() == null) ? "." : getValue().toString();
		return getChr() + "\tSpotArray\tfeature\t" + low() + "\t" + high() + "\t" + valueStr + "\t" + strand() + "\t.\tprobe_id=" + idStr + ";count=1";
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	

	/**
	 * @param id the id to set
	 */
	public void setId(final String id) {
		this.id = id;
	}
	

	/**
	 * @return the value
	 */
	public Number getValue() {
		return value;
	}
	

	/**
	 * @param value the value to set
	 */
	public void setValue(final Number value) {
		this.value = value;
	}
}

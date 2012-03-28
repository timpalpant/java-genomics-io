package edu.unc.genomics;

public class ValuedInterval extends Interval {
	private static final long serialVersionUID = 2976113135643438146L;
	protected String id;
	protected Double value;
	
	public ValuedInterval(String chr, int start, int stop, String id, Double value) {
		super(chr, start, stop);
		this.id = id;
		this.value = value;
	}
	
	public ValuedInterval(String chr, int start, int stop, String id) {
		this(chr, start, stop, id, null);
	}
	
	public ValuedInterval(String chr, int start, int stop) {
		this(chr, start, stop, null);
	}
	
	public ValuedInterval(String chr, int start) {
		this(chr, start, 0);
	}

	public ValuedInterval(String chr) {
		this(chr, 0);
	}
	
	public ValuedInterval() {
		this(null);
	}
	
	@Override
	public String toBed() {
		String idStr = (id == null) ? "." : id;
		String valueStr = (getValue() == null) ? "." : getValue().toString();
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
	public Double getValue() {
		return value;
	}
	

	/**
	 * @param value the value to set
	 */
	public void setValue(final Double value) {
		this.value = value;
	}
}

/**
 * 
 */
package edu.unc.genomics;

import edu.unc.genomics.io.IntervalFileFormatException;

/**
 * @author timpalpant
 *
 */
public class GFFEntry extends ValuedInterval {
	private static final long serialVersionUID = 8657984166944604756L;
	
	private String source;
	private String feature;
	private String frame;

	/**
	 * @param chr
	 * @param start
	 * @param stop
	 */
	public GFFEntry(String chr, int start, int stop) {
		super(chr, start, stop);
	}
	
	public static GFFEntry parse(String line) {
		if (line.startsWith("#") || line.startsWith("track")) {
			return null;
		}
		
		String[] entry = line.split("\t");
		if (entry.length < 9) {
			throw new IntervalFileFormatException("Invalid GFF entry has < 9 columns");
		}
		
		String chr = entry[0];
		int start = Integer.parseInt(entry[3]);
		int stop = Integer.parseInt(entry[4]);
		String strand = entry[6];
		
		GFFEntry gff = new GFFEntry(chr, start, stop);
		gff.setSource(entry[1]);
		gff.setFeature(entry[2]);
		if (!entry[5].equals(".")) {
			gff.setValue(Double.valueOf(entry[5]));
		}
		gff.setFrame(entry[7]);
		String[] note = entry[8].split(";");
		String id = note[0].substring(9);
		gff.setId(id);
		
		int tmpLow = gff.low();
		int tmpHigh = gff.high();
		if (strand.equalsIgnoreCase("+")) {
			gff.setStart(tmpLow);
			gff.setStop(tmpHigh);
		} else if (strand.equalsIgnoreCase("-")) {
			gff.setStart(tmpHigh);
			gff.setStop(tmpLow);
		}
		
		return gff;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the feature
	 */
	public String getFeature() {
		return feature;
	}

	/**
	 * @param feature the feature to set
	 */
	public void setFeature(String feature) {
		this.feature = feature;
	}

	/**
	 * @return the frame
	 */
	public String getFrame() {
		return frame;
	}

	/**
	 * @param frame the frame to set
	 */
	public void setFrame(String frame) {
		this.frame = frame;
	}

}

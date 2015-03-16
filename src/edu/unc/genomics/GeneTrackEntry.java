package edu.unc.genomics;

import edu.unc.genomics.io.IntervalFileFormatException;

/**
 * A GeneTrack index file. Value corresponds to forward+reverse
 * 
 * @author timpalpant
 *
 */
public class GeneTrackEntry extends ValuedInterval {
  private static final long serialVersionUID = 8657984166944604756L;

  private double forward;
  private double reverse;

  /**
   * @param chr
   * @param start
   * @param stop
   */
  public GeneTrackEntry(String chr, int start, int stop) {
    super(chr, start, stop);
  }

  public static GeneTrackEntry parse(String line) {
    if (line.startsWith("#") || line.startsWith("track") || line.startsWith("chrom")) {
      return null;
    }

    String[] entry = line.split("\t");
    if (entry.length < 4) {
      throw new IntervalFileFormatException("Invalid GeneTrack entry has < 4 columns: " + line);
    }

    String chr = entry[0];
    int pos = Integer.parseInt(entry[1]);
    GeneTrackEntry gt = new GeneTrackEntry(chr, pos, pos);
    gt.setForward(Double.parseDouble(entry[2]));
    gt.setReverse(Double.parseDouble(entry[3]));

    return gt;
  }

  @Override
  public Double getValue() {
    return forward + reverse;
  }

  @Override
  public void setValue(final Number value) {
    this.forward = value.doubleValue();
    this.reverse = 0;
  }

  /**
   * @return the forward
   */
  public double getForward() {
    return forward;
  }

  /**
   * @param forward
   *          the forward to set
   */
  public void setForward(double forward) {
    this.forward = forward;
  }

  /**
   * @return the reverse
   */
  public double getReverse() {
    return reverse;
  }

  /**
   * @param reverse
   *          the reverse to set
   */
  public void setReverse(double reverse) {
    this.reverse = reverse;
  }

}

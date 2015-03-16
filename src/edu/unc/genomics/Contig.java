package edu.unc.genomics;

import java.util.Arrays;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.util.ArithmeticUtils;

/**
 * A contiguous block of values in the genome
 * 
 * @author timpalpant
 *
 */
public class Contig extends Interval {

  private static final long serialVersionUID = -4260411310249231783L;

  private final float[] values;
  private SummaryStatistics stats;

  public Contig(Interval interval) {
    this(interval, null);
  }

  public Contig(Interval interval, float[] values) throws ContigException {
    this(interval.getChr(), interval.getStart(), interval.getStop(), values);
  }

  /**
   * Create a new Contig for the interval chr:start-stop with values in values[]
   * 
   * @param chr
   *          the chromosome of the interval
   * @param start
   *          the start base pair of the interval
   * @param stop
   *          the stop base pair of the interval
   * @param values
   *          the values for this interval, one for each base pair. The value
   *          for start should be in values[0], start+/-1 in values[1], etc.
   * @throws ContigException
   *           if values.length != (start-stop+1)
   */
  public Contig(String chr, int start, int stop, float[] values) throws ContigException {
    super(chr, start, stop);

    // Verify that values has the correct length
    if (values == null) {
      values = new float[length()];
      Arrays.fill(values, Float.NaN);
    } else if (values.length != length()) {
      throw new ContigException("Incorrect number of values for Contig (" + values.length + " != " + length() + ")");
    }
    this.values = values;
  }

  public Contig(String chr, int start, int stop) {
    this(chr, start, stop, null);
  }

  /**
   * Copy a subset of data from this Contig into a new Contig. If the Contig is
   * not for this interval, then null is returned. If the specified start-stop
   * are outside the Contig range, then the returned array will be padded with
   * NaNs.
   * 
   * @param i
   *          the interval to get data for
   * @return the data for Interval i, or NaN where data is not available
   */
  public Contig copy(Interval i) {
    if (!i.getChr().equals(getChr())) {
      return null;
    }

    return copy(i.getStart(), i.getStop());
  }

  /**
   * Copy a subset of data from this Contig into a new Contig. If the specified
   * start-stop are outside the Contig range, then the returned array will be
   * padded with NaNs.
   * 
   * @param start
   *          the first base pair
   * @param stop
   *          the last base pair
   * @return the data from start-stop, or NaN where data is not available
   */
  public Contig copy(int start, int stop) {
    return new Contig(getChr(), start, stop, get(start, stop));
  }

  /**
   * @return the values
   */
  public float[] getValues() {
    return values;
  }

  /**
   * Get data from this Contig. If the Contig is not for this interval, then
   * null is returned. If the specified start-stop are outside the Contig range,
   * then the returned array will be padded with NaNs.
   * 
   * @param i
   *          the interval to get data for
   * @return the data for Interval i, or NaN where data is not available
   */
  public float[] get(Interval i) {
    if (!i.getChr().equals(getChr())) {
      return null;
    }

    return get(i.getStart(), i.getStop());
  }

  /**
   * Get data from this Contig. If the specified start-stop are outside the
   * Contig range, then the returned array will be padded with NaNs.
   * 
   * @param start
   *          the first base pair
   * @param stop
   *          the last base pair
   * @return the data from start-stop, or NaN where data is not available
   */
  public float[] get(int start, int stop) {
    int length = Math.abs(stop - start) + 1;
    float[] result = new float[length];
    int dir = (start <= stop) ? 1 : -1;
    int bp = start;
    for (int i = 0; i < result.length; i++) {
      result[i] = get(bp + dir * i);
    }
    return result;
  }

  /**
   * Get a single value from this Contig, or NaN if there is no data for the
   * base pair
   * 
   * @param bp
   *          a base pair to get data for
   * @return the value of this base pair
   */
  public float get(int bp) {
    if (!includes(bp)) {
      return Float.NaN;
    }

    int i = Math.abs(bp - getStart());
    return values[i];
  }

  /**
   * Set a certain range of this Contig to have a certain value
   * 
   * @param interval
   *          an interval with a value to set
   * @throws ContigException
   *           if the interval is not within this Contig
   */
  public void set(ValuedInterval interval) throws ContigException {
    set(interval, interval.getValue().floatValue());
  }

  /**
   * Set a certain range of this Contig to have a certain value
   * 
   * @param interval
   *          the range to set
   * @param value
   *          the value to set
   * @throws ContigException
   *           if the interval is not within this Contig
   */
  public void set(Interval interval, float value) throws ContigException {
    set(interval.getStart(), interval.getStop(), value);
  }

  /**
   * Set the base pairs start-stop to have a certain value
   * 
   * @param start
   *          the first base pair in an interval
   * @param stop
   *          the last base pair in an interval
   * @param value
   *          the value to set this range to
   * @throws ContigException
   *           if start-stop is not within this Contig
   */
  public void set(int start, int stop, float value) throws ContigException {
    int low = Math.min(start, stop);
    int high = Math.max(start, stop);
    for (int bp = low; bp <= high; bp++) {
      set(bp, value);
    }
  }

  /**
   * Set a base pair in this Contig to a specific value
   * 
   * @param bp
   *          the base pair to set
   * @param value
   *          the value to set for bp
   * @throws ContigException
   *           if bp is not within this Contig
   */
  public void set(int bp, float value) throws ContigException {
    if (!includes(bp)) {
      throw new ContigException(bp + " is outside the range of this Contig");
    }

    values[bp - low()] = value;
    // Recompute stats if they have previously been computed
    if (stats != null) {
      computeStats();
    }
  }

  /**
   * Get summary statistics for the result of this Wig query
   * 
   * @return SummaryStatistics for the data
   */
  public SummaryStatistics getStats() {
    if (stats == null) {
      computeStats();
    }

    return stats;
  }

  private void computeStats() {
    stats = new SummaryStatistics();
    for (float v : values) {
      if (!Float.isNaN(v) && !Float.isInfinite(v)) {
        stats.addValue(v);
      }
    }
  }

  /**
   * Get the number of base pairs with data in a Wig query
   * 
   * @return the number of base pairs with data in iter between start-stop
   */
  public long coverage() {
    return numBases();
  }

  /**
   * Get the number of base pairs with data in a Wig query
   * 
   * @return the number of base pairs with data in iter between start-stop
   */
  public long numBases() {
    return getStats().getN();
  }

  /**
   * Get the sum of all values in a Wig query
   * 
   * @return the sum of all values for base pairs with data in iter between
   *         start-stop
   */
  public float total() {
    return (float) getStats().getSum();
  }

  /**
   * Get the mean of all values in a Wig query
   * 
   * @return the mean of all base pairs with data in iter between start-stop
   */
  public float mean() {
    return (float) getStats().getMean();
  }

  /**
   * Get the standard deviation of values a Wig query
   * 
   * @return the standard deviation of base pairs with data in iter between
   *         start-stop
   */
  public float stdev() {
    return (float) Math.sqrt(getStats().getPopulationVariance());
  }

  /**
   * Get the minimum value in a Wig query
   * 
   * @return the minimum value of the data in iter between start-stop
   */
  public float min() {
    return (float) getStats().getMin();
  }

  /**
   * Get the maximum value in a Wig query
   * 
   * @return the minimum value of the data in iter between start-stop
   */
  public float max() {
    return (float) getStats().getMax();
  }

  /**
   * Get a fixedStep header for this Contig
   * 
   * @return a fixedStep header line for a Wig file
   */
  public String getFixedStepHeader() {
    int actualSpan = Math.min(getMinSpan(), getMinStep());
    return Contig.Type.FIXEDSTEP.getId() + " chrom=" + getChr() + " start=" + getFirstBaseWithData() + " span="
        + actualSpan + " step=" + getMinStep();
  }

  /**
   * Get a variableStep header for this Contig
   * 
   * @return a variableStep header line for a Wig file
   */
  public String getVariableStepHeader() {
    return Contig.Type.VARIABLESTEP.getId() + " chrom=" + getChr() + " span=" + getVariableStepSpan();
  }

  /**
   * Find the lowest base pair in this Contig that actually has a data value
   * (not NaN)
   * 
   * @return the first base pair with data
   */
  public int getFirstBaseWithData() {
    for (int bp = low(); bp <= high(); bp++) {
      if (!Float.isNaN(get(bp))) {
        return bp;
      }
    }

    return high();
  }

  /**
   * Calculate the shortest span of values in this Contig A Wig file with this
   * span will still resolve all of the data in this contig e.g. single base
   * pair resolution = 1 but if really the data are in 5bp windows, then should
   * return 5 since 5 values will be duplicates and don't need to be written
   * individually
   * 
   * @return the minimum span size for this Contig
   */
  public int getMinSpan() {
    int minSpan = length();
    int span = 1;
    int firstbp = getFirstBaseWithData();
    float prevValue = get(firstbp);
    for (int bp = firstbp + 1; bp <= high(); bp++) {
      float value = get(bp);
      if (value == prevValue) {
        span++;
      } else {
        if (!Float.isNaN(prevValue) && span < minSpan) {
          minSpan = span;
        }

        if (minSpan == 1) {
          break;
        }

        prevValue = value;
        span = 1;
      }
    }

    return minSpan;
  }

  /**
   * Pass through the data again to check that all values can be resolved with
   * minSpan For an example why this is necessary, see the unit test
   * VariableStepContigTest Essentially, all spans must be integer multiples of
   * minSpan, or else we have to shrink to the greatest common denominator.
   * 
   * @return the span size that must be used if this Contig is written in
   *         variableStep format
   */
  public int getVariableStepSpan() {
    int minSpan = getMinSpan();
    int span = 1;
    int firstbp = getFirstBaseWithData();
    float prevValue = get(firstbp);
    for (int bp = firstbp + 1; bp <= high(); bp++) {
      float value = get(bp);
      if (value == prevValue) {
        span++;
      } else {
        if (!Float.isNaN(prevValue)) {
          if (span % minSpan > 0) {
            minSpan = ArithmeticUtils.gcd(span, minSpan);
          }
        }

        if (minSpan == 1) {
          break;
        }

        prevValue = value;
        span = 1;
      }
    }

    return minSpan;
  }

  /**
   * Calculate the minimum step size of values in this Contig
   * 
   * @return the minimum step size for this contig
   */
  public int getMinStep() {
    if (!isFixedStep()) {
      return 1;
    }

    // Since the Contig must have fixed step size, find the first step size
    return getFirstStep();
  }

  /**
   * Get the step size between the first two unique data values
   * 
   * @return the step (in bp) from the first data value to the second data value
   */
  private int getFirstStep() {
    int firstbp = getFirstBaseWithData();
    float firstValue = get(firstbp);
    boolean passedNaN = false;
    int bp;
    for (bp = firstbp + 1; bp <= high(); bp++) {
      float nextValue = get(bp);
      if (Float.isNaN(nextValue)) {
        passedNaN = true;
      } else if (nextValue != firstValue || passedNaN) {
        break;
      }
    }

    return bp - firstbp;
  }

  /**
   * @return true if this Contig is fixedStep, i.e. regularly spaced data values
   */
  public boolean isFixedStep() {
    int bp = getFirstBaseWithData();
    int prevbp = bp;
    float prevValue = get(prevbp);
    int firstStep = getFirstStep();
    while (bp < high()) {
      boolean passedNaN = false;
      for (bp = prevbp + 1; bp <= high(); bp++) {
        float nextValue = get(bp);
        if (Float.isNaN(nextValue)) {
          passedNaN = true;
        } else if (nextValue != prevValue || passedNaN) {
          break;
        }
      }

      if (bp > high()) {
        break;
      }

      int step = bp - prevbp;
      // Can have multiple spans of the same value in a row
      if (step % firstStep > 0) {
        return false;
      }

      prevbp = bp;
      prevValue = get(bp);
    }

    return true;
  }

  /**
   * An enumeration representing the different types of Contigs
   * 
   * @author timpalpant
   *
   */
  public static enum Type {
    FIXEDSTEP("fixedStep"), VARIABLESTEP("variableStep");

    private final String id;

    Type(final String id) {
      this.id = id;
    }

    public static Type forId(final String id) {
      for (Type c : Type.values()) {
        if (c.getId().equals(id)) {
          return c;
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

}

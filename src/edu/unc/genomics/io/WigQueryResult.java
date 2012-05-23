package edu.unc.genomics.io;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.broad.igv.bbfile.WigItem;

import edu.unc.genomics.Interval;

/**
 * Wraps the result of a query from a Wig file
 * TODO Consider rewriting this class to have custom subclasses for BigWig/TextWig files
 * and do away with Iterator<WigItem> that probably has unnecessary overhead
 * 
 * @author timpalpant
 *
 */
public class WigQueryResult {
	
	private final Iterator<WigItem> iter;
	private final Interval interval;
	
	private float[] flattened;
	private SummaryStatistics stats;
	
	WigQueryResult(Iterator<WigItem> iter, Interval interval) {
		this.iter = iter;
		this.interval = interval;
	}
	
	/**
	 * Collect all of the results from the query and return them flattened into an array with single base pair resolution
	 * If the query interval was Crick (start > stop), then the array will be reversed prior to returning
	 * NOTE: A reference is returned to a single copy of the flattened results. Modifications to the returned
	 * array will be permanent. Clone the array if you wish to preserve the original values.
	 * 
	 * @return data a flattened array of the data
	 */
	public float[] getFlattened() {
		if (flattened == null) {
			collect();
		}
		
		return flattened;
	}
	
	/**
	 * Get data from this result. If the specified start-stop are outside the original query range,
	 * then the returned array will be padded with NaNs.
	 * @param start the first base pair
	 * @param stop the last base pair
	 * @return the data from start-stop, or NaN where data is not available
	 */
	public float[] get(int start, int stop) {
		int length = Math.abs(stop-start) + 1;
		float[] result = new float[length];
		int dir = (start <= stop) ? 1 : -1;
		int bp = start;
		for (int i = 0; i < result.length; i++) {
			result[i] = get(bp+dir*i);
		}
		return result;
	}
	
	/**
	 * Get a single value from this query, or NaN if there is no data for the base pair
	 * @param bp a base pair to get data for
	 * @return the value of this base pair
	 */
	public float get(int bp) {
		if (!interval.includes(bp)) {
			return Float.NaN;
		}
		
		int i = Math.abs(bp-interval.getStart());
		return getFlattened()[i];
	}
	
	/**
	 * Get summary statistics for the result of this Wig query
	 * @return SummaryStatistics for the data
	 */
	public SummaryStatistics stats() {
		if (stats == null) {
			collect();
		}
		
		return stats;
	}
	
	/**
	 * Get the number of base pairs with data in a Wig query
	 * @return the number of base pairs with data in iter between start-stop
	 */
	public long coverage() {
		return numBases();
	}
	
	/**
	 * Get the number of base pairs with data in a Wig query
	 * @return the number of base pairs with data in iter between start-stop
	 */
	public long numBases() {
		return stats().getN();
	}
	
	/**
	 * Get the sum of all values in a Wig query
	 * @return the sum of all values for base pairs with data in iter between start-stop
	 */
	public float total() {
		return (float) stats().getSum();
	}
	
	/**
	 * Get the mean of all values in a Wig query
	 * @return the mean of all base pairs with data in iter between start-stop
	 */
	public float mean() {
		return (float) stats().getMean();
	}
	
	/**
	 * Get the standard deviation of values a Wig query
	 * @return the standard deviation of base pairs with data in iter between start-stop
	 */
	public float stdev() {
		return (float) Math.sqrt(stats().getPopulationVariance());
	}
	
	/**
	 * Get the minimum value in a Wig query
	 * @return the minimum value of the data in iter between start-stop
	 */
	public float min() {
		return (float) stats().getMin();
	}
	
	/**
	 * Get the maximum value in a Wig query
	 * @return the minimum value of the data in iter between start-stop
	 */
	public float max() {
		return (float) stats().getMax();
	}

	/**
	 * Get the interval that was queried
	 * @return the interval for this query
	 */
	public Interval getInterval() {
		return interval;
	}
	
	/**
	 * Actually load all of the data from the query, flatten into an array, and compute descriptive 
	 * statistics while we load it
	 */
	private void collect() {
		stats = new SummaryStatistics();
		flattened = new float[interval.length()];
		Arrays.fill(flattened, Float.NaN);
		
		while (iter.hasNext()) {
			WigItem item = iter.next();
			float value = item.getWigValue();
			if (!Float.isNaN(value)) {
				for (int bp = item.getStartBase(); bp <= item.getEndBase(); bp++) {
					if (interval.includes(bp)) {
						flattened[bp-interval.low()] = value;
						stats.addValue(value);
					}
				}
			}
		}
		
		if (interval.isCrick()) {
			ArrayUtils.reverse(flattened);
		}
	}
}

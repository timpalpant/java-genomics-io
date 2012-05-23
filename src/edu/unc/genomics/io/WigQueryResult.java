package edu.unc.genomics.io;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.broad.igv.bbfile.WigItem;

import edu.unc.genomics.Interval;

/**
 * Wraps the result of a query from a Wig file. Data is lazy-loaded from the 
 * supplied iterator.
 * 
 * @author timpalpant
 *
 */
public class WigQueryResult {
	
	private final Iterator<WigItem> iter;
	private final Interval interval;
	
	private float[] flattened;
	private SummaryStatistics stats;
	
	public WigQueryResult(Iterator<WigItem> iter, Interval interval) {
		this.iter = iter;
		this.interval = interval;
	}
	
	/**
	 * Collect all of the results from the query and return them flattened into an array
	 * If the query interval was Crick (start > stop), then the array will be reversed prior to returning
	 * NOTE: A reference is returned to a single copy of the flattened results. Modifications to the returned
	 * array will be permanent. Clone the array if you wish to preserve the original values.
	 * 
	 * @return data a flattened array of the data
	 */
	public float[] flattened() {
		if (flattened == null) {
			collect();
		}
		
		return flattened;
	}
	
	public float[] getSubset(int start, int stop) throws WigFileException {
		int low = Math.min(start, stop);
		int high = Math.max(start, stop);
		if (low < interval.low() || high > interval.high()) {
			throw new WigFileException("WigQueryResult does not contain data for base pairs "+start+"-"+stop);
		}
		
		// We have the data in flattened() from interval.getStart() - interval.getStop()
		// Need to extract the subset from start-stop and reverse as necessary
		int distToStart = Math.abs(start-interval.getStart());
		int distToStop = Math.abs(stop-interval.getStart());
		int from = Math.min(distToStart, distToStop);
		int to = Math.max(distToStart, distToStop);
		float[] subset = Arrays.copyOfRange(flattened(), from, to+1);
		
		// If this subset query is in the opposite direction of the original query
		if (distToStart > distToStop) {
			ArrayUtils.reverse(subset);
		}
		
		return subset;
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
				for (int i = item.getStartBase(); i <= item.getEndBase(); i++) {
					if (i >= interval.low() && i <= interval.high()) {
						flattened[i-interval.low()] = value;
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

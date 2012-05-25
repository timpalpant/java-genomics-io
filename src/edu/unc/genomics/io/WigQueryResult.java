package edu.unc.genomics.io;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;

import edu.unc.genomics.Contig;
import edu.unc.genomics.Interval;
import edu.unc.genomics.WigEntry;

/**
 * Loads the result of a query from a Wig file as a Contig
 * 
 * @author timpalpant
 *
 */
public class WigQueryResult extends Contig {
	
	private static final long serialVersionUID = 2066295662711901691L;
	
	WigQueryResult(Interval interval, Iterator<WigEntry> iter) throws RuntimeException {
		super(interval, collect(interval, iter));
	}
	
	/**
	 * Actually load all of the data from the query, flatten into an array, and compute descriptive 
	 * statistics while we load it
	 */
	private static float[] collect(Interval interval, Iterator<WigEntry> iter) {
		float[] values = new float[interval.length()];
		Arrays.fill(values, Float.NaN);
		
		while (iter.hasNext()) {
			WigEntry item = iter.next();
			float value = item.getValue().floatValue();
			if (!Float.isNaN(value)) {
				for (int bp = item.low(); bp <= item.high(); bp++) {
					if (interval.includes(bp)) {
						values[bp-interval.low()] = value;
					}
				}
			}
		}
		
		if (interval.isCrick()) {
			ArrayUtils.reverse(values);
		}
		
		return values;
	}
}

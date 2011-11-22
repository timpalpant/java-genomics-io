package edu.unc.genomics;

/**
 * Factory for parsing Intervals from Strings
 * 
 * @author timpalpant
 *
 * @param <T>
 */
public interface IntervalFactory<T extends Interval> {
	
	/**
	 * Parse an interval from a String
	 * @param line the entry to parse
	 * @return a new Interval of type T
	 */
	T parse(String line);
	
}

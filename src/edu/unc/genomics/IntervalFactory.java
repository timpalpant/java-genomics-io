package edu.unc.genomics;

import net.sf.samtools.TabixWriter;

/**
 * Factory for parsing Intervals from Strings
 * 
 * @author timpalpant
 *
 * @param <T> the type of Interval that this IntervalFactory produces
 */
public interface IntervalFactory<T extends Interval> {
	
	/**
	 * Parse an interval from a String
	 * @param line the entry to parse
	 * @return a new Interval of type T
	 */
	T parse(String line);
	
	/**
	 * Return the appropriate Tabix configuration for this format
	 * @return a configuration object for Tabix
	 */
	TabixWriter.Conf tabixConf();
	
}

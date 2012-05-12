package edu.unc.genomics.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;
import org.broad.igv.bbfile.WigItem;

import edu.unc.genomics.Interval;

/**
 * The base class for ASCII-text Wig files and binary BigWig files.
 * 
 * BigWig and regular ASCII-text Wig files may be used interchangeably as the base class WigFile,
 * and the correct format (Wig/BigWig) can be autodetected by calling WigFile.autodetect()
 * 
 * @author timpalpant
 *
 */
public abstract class WigFile implements Closeable {
	
	private static final Logger log = Logger.getLogger(WigFile.class);
	protected final Path p;
	
	protected WigFile(Path p) {
		this.p = p;
	}
	
	/**
	 * Autodetect whether a file is an ASCII-text Wig file or a BigWig file and initialize it
	 * @param p the file to initialize
	 * @return a WigFile handle to p that is the appropriate subclass (BigWigFile or TextWigFile)
	 * @throws IOException
	 * @throws WigFileException
	 */
	public static WigFile autodetect(Path p) throws IOException, WigFileException {
		WigFile wig;
		
		if (BigWigFile.isBigWig(p)) {
			log.debug("Autodetected BigWig file type for: " + p.getFileName());
			wig = new BigWigFile(p);
		} else {
			log.debug("Autodetected Wiggle file type for: " + p.getFileName());
			wig = new TextWigFile(p);
		}
		
		return wig;
	}
	
	/**
	 * Collect all of the WigItems provided by iter and flatten them into an array
	 * If start > stop, then the array will be reversed prior to returning
	 * 
	 * @param iter an iterator with Wig data
	 * @param start the start base pair, corresponding to data[0]
	 * @param stop the stop base pair, corresponding to data[data.length-1]
	 * @param defaultValue the value to use where there is no data
	 * @return data a flattened array of the data from iter
	 */
	public static float[] flattenData(Iterator<WigItem> iter, int start, int stop, float defaultValue) {
		int low = Math.min(start, stop);
		int high = Math.max(start, stop);
		int length = high - low + 1;
		float[] data = new float[length];
		Arrays.fill(data, defaultValue);
		
		while (iter.hasNext()) {
			WigItem item = iter.next();
			for (int i = item.getStartBase(); i <= item.getEndBase(); i++) {
				if (i >= low && i <= high) {
					data[i-low] = item.getWigValue();
				}
			}
		}
		
		if (start > stop) {
			ArrayUtils.reverse(data);
		}
		
		return data;
	}
	
	/**
	 * Collect all of the WigItems provided by iter and flatten them into an array
	 * If start > stop, then the array will be reversed prior to returning
	 * 
	 * @param iter an iterator with Wig data
	 * @param start the start base pair, corresponding to data[0]
	 * @param stop the stop base pair, corresponding to data[data.length-1]
	 * @return data a flattened array of the data from iter
	 */
	public static float[] flattenData(Iterator<WigItem> iter, int start, int stop) {
		return flattenData(iter, start, stop, Float.NaN);
	}
	
	/**
	 * Get summary statistics for the result of a Wig query
	 * @param iter an iterator with the result of a Wig query
	 * @param start the range to use for computing statistics (values for bases outside this range will be ignored)
	 * @param stop the range to use for computing statistics (values for bases outside this range will be ignored)
	 * @return SummaryStatistics for the data in iter between start-stop
	 */
	public static SummaryStatistics stats(Iterator<WigItem> iter, int start, int stop) {
		int low = Math.min(start, stop);
		int high = Math.max(start, stop);
		SummaryStatistics stats = new SummaryStatistics();
		while (iter.hasNext()) {
			WigItem item = iter.next();
			float value = item.getWigValue();
			if (!Float.isNaN(value) && !Float.isInfinite(value)) {
				for (int i = item.getStartBase(); i <= item.getEndBase(); i++) {
					if (i >= low && i <= high) {
						stats.addValue(value);
					}
				}
			}
		}
		
		return stats;
	}
	
	/**
	 * Get the number of base pairs with data in a Wig query
	 * @param iter an iterator with the result of a Wig query
	 * @param start the range to use for computing statistics (values for bases outside this range will be ignored)
	 * @param stop the range to use for computing statistics (values for bases outside this range will be ignored)
	 * @return the number of base pairs with data in iter between start-stop
	 */
	public static long numBases(Iterator<WigItem> iter, int start, int stop) {
		return stats(iter, start, stop).getN();
	}
	
	/**
	 * Get the sum of all values in a Wig query
	 * @param iter an iterator with the result of a Wig query
	 * @param start the range to use for computing statistics (values for bases outside this range will be ignored)
	 * @param stop the range to use for computing statistics (values for bases outside this range will be ignored)
	 * @return the sum of all values for base pairs with data in iter between start-stop
	 */
	public static float total(Iterator<WigItem> iter, int start, int stop) {
		return (float) stats(iter, start, stop).getSum();
	}
	
	/**
	 * Get the mean of all values in a Wig query
	 * @param iter an iterator with the result of a Wig query
	 * @param start the range to use for computing statistics (values for bases outside this range will be ignored)
	 * @param stop the range to use for computing statistics (values for bases outside this range will be ignored)
	 * @return the mean of all base pairs with data in iter between start-stop
	 */
	public static float mean(Iterator<WigItem> iter, int start, int stop) {
		return (float) stats(iter, start, stop).getMean();
	}
	
	/**
	 * Get the standard deviation of values a Wig query
	 * @param iter an iterator with the result of a Wig query
	 * @param start the range to use for computing statistics (values for bases outside this range will be ignored)
	 * @param stop the range to use for computing statistics (values for bases outside this range will be ignored)
	 * @return the standard deviation of base pairs with data in iter between start-stop
	 */
	public static float stdev(Iterator<WigItem> iter, int start, int stop) {
		return (float) Math.sqrt(stats(iter, start, stop).getPopulationVariance());
	}
	
	/**
	 * Get the minimum value in a Wig query
	 * @param iter an iterator with the result of a Wig query
	 * @param start the range to use for computing statistics (values for bases outside this range will be ignored)
	 * @param stop the range to use for computing statistics (values for bases outside this range will be ignored)
	 * @return the minimum value of the data in iter between start-stop
	 */
	public static float min(Iterator<WigItem> iter, int start, int stop) {
		return (float) stats(iter, start, stop).getMin();
	}
	
	/**
	 * Get the maximum value in a Wig query
	 * @param iter an iterator with the result of a Wig query
	 * @param start the range to use for computing statistics (values for bases outside this range will be ignored)
	 * @param stop the range to use for computing statistics (values for bases outside this range will be ignored)
	 * @return the minimum value of the data in iter between start-stop
	 */
	public static float max(Iterator<WigItem> iter, int start, int stop) {
		return (float) stats(iter, start, stop).getMax();
	}
	
	public Path getPath() {
		return p;
	}
	
	/**
	 * Query for data in this Wig file that overlaps a specific interval
	 * @param i the Interval of data to query for
	 * @return an Iterator of WigItems that overlap the Interval i
	 * @throws IOException if a disk read error occurs
	 * @throws WigFileException if the Wig file does not contain data for this Interval
	 */
	public Iterator<WigItem> query(Interval i) throws IOException, WigFileException {
		return query(i.getChr(), i.low(), i.high());
	}
	
	/**
	 * Query for data in this Wig file that overlaps a specific interval
	 * @param chr the chromosome of the interval
	 * @param low the lowest base pair of the interval
	 * @param high the highest base pair of the interval
	 * @return an Iterator of WigItems that overlap the specified interval
	 * @throws IOException if a disk read error occurs
	 * @throws WigFileException if the Wig file does not contain data for this Interval
	 */
	public abstract Iterator<WigItem> query(String chr, int low, int high) throws IOException, WigFileException;
	
	/**
	 * @return the set of all chromosomes in this Wig file
	 */
	public abstract Set<String> chromosomes();
	
	/**
	 * @param chr the chromosome to get the start base pair for
	 * @return the first base pair with data for chr in this Wig file
	 */
	public abstract int getChrStart(String chr);
	
	/**
	 * @param chr the chromosome to get the stop base pair for
	 * @return the last base pair with data for chr in this Wig file
	 */
	public abstract int getChrStop(String chr);
	
	/**
	 * Does this Wig file include data for a given Interval?
	 * @param i the Interval to query for
	 * @return true if this Wig file includes data for i
	 */
	public boolean includes(Interval i) {
		return includes(i.getChr(), i.getStart(), i.getStop());
	}
	
	/**
	 * Does this Wig file include data for a given interval?
	 * @param chr the chromosome to query for
	 * @param start the start of the interval
	 * @param stop the stop of the interval
	 * @return true if this Wig file includes data for chr:start-stop
	 */
	public abstract boolean includes(String chr, int start, int stop);
	
	/**
	 * Does this Wig file include data for a given chromosome?
	 * @param chr the chromosome to query for
	 * @return true if this Wig file includes data for chr
	 */
	public abstract boolean includes(String chr);
	
	/**
	 * @return the number of base pairs with data values in this Wig file
	 */
	public abstract long numBases();
	
	/**
	 * @return the sum of all values for base pairs with data in this Wig file
	 */
	public abstract double total();
	
	/**
	 * @return the mean of all values for base pairs with data in this Wig file
	 */
	public abstract double mean();
	
	/**
	 * @return the standard deviation of all values for base pairs with data in this Wig file
	 */
	public abstract double stdev();
	
	/**
	 * @return the minimum value in this Wig file
	 */
	public abstract double min();
	
	/**
	 * @return the maximum value in this Wig file
	 */
	public abstract double max();
	
	@Override
	public abstract String toString();
}

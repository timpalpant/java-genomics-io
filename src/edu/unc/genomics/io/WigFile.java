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

public abstract class WigFile implements Closeable {
	
	private static final Logger log = Logger.getLogger(WigFile.class);
	protected final Path p;
	
	protected WigFile(Path p) {
		log.debug("Initializing wig file: " + p.getFileName());
		this.p = p;
	}
	
	/**
	 * @param p
	 * @return
	 * @throws IOException
	 * @throws WigFileException
	 */
	public static WigFile autodetect(Path p) throws IOException, WigFileException {
		WigFile wig;
		
		if (BigWigFile.isBigWig(p)) {
			log.debug("Autodetected BigWig file type for: " + p);
			wig = new BigWigFile(p);
		} else {
			log.debug("Autodetected Wiggle file type for: " + p);
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
	
	public static float mean(Iterator<WigItem> iter, int start, int stop) {
		return (float) stats(iter, start, stop).getMean();
	}
	
	public static float stdev(Iterator<WigItem> iter, int start, int stop) {
		return (float) Math.sqrt(stats(iter, start, stop).getPopulationVariance());
	}
	
	public static float min(Iterator<WigItem> iter, int start, int stop) {
		return (float) stats(iter, start, stop).getMin();
	}
	
	public static float max(Iterator<WigItem> iter, int start, int stop) {
		return (float) stats(iter, start, stop).getMax();
	}
	
	public Path getPath() {
		return p;
	}
	
	public Iterator<WigItem> query(Interval i) throws IOException, WigFileException {
		return query(i.getChr(), i.low(), i.high());
	}
	
	public abstract Iterator<WigItem> query(String chr, int low, int high) throws IOException, WigFileException;
	
	public abstract Set<String> chromosomes();
	
	public abstract int getChrStart(String chr);
	
	public abstract int getChrStop(String chr);
	
	public boolean includes(Interval i) {
		return includes(i.getChr(), i.getStart(), i.getStop());
	}
	
	public abstract boolean includes(String chr, int start, int stop);
	
	public abstract boolean includes(String chr);
	
	public abstract long numBases();
	
	public abstract double total();
	
	public abstract double mean();
	
	public abstract double stdev();
	
	public abstract double min();
	
	public abstract double max();
	
	public abstract String toString();
}

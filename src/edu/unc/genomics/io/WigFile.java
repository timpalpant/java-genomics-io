package edu.unc.genomics.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
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
			wig = new BigWigFile(p);
		} else {
			wig = new TextWigFile(p);
		}
		
		return wig;
	}
	
	/**
	 * @param iter
	 * @return
	 */
	public static float[] flattenData(Iterator<WigItem> iter, int start, int stop) {
		int low = Math.min(start, stop);
		int high = Math.max(start, stop);
		int length = high - low + 1;
		float[] data = new float[length];
		Arrays.fill(data, Float.NaN);
		
		while (iter.hasNext()) {
			WigItem item = iter.next();
			for (int i = item.getStartBase(); i <= item.getEndBase(); i++) {
				if (i-start > 0 && i-start < data.length) {
					data[i-start] = item.getWigValue();
				}
			}
		}
		
		if (start > stop) {
			ArrayUtils.reverse(data);
		}
		
		return data;
	}
	
	public Path getPath() {
		return p;
	}
	
	public Iterator<WigItem> query(Interval i) throws IOException, WigFileException {
		return query(i.getChr(), i.low(), i.high());
	}
	
	public abstract Iterator<WigItem> query(String chr, int start, int stop) throws IOException, WigFileException;
	
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

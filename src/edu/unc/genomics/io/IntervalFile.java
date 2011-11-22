package edu.unc.genomics.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

import edu.unc.genomics.Interval;

/**
 * @author timpalpant
 *
 */
public abstract class IntervalFile<T extends Interval> implements Iterable<T>, Closeable {
	
	protected Path p;
	
	protected IntervalFile(Path p) {
		this.p = p;
	}
	
	public static IntervalFile<? extends Interval> autodetect(Path p) throws IntervalFileSnifferException, IOException {
		IntervalFileSniffer sniffer = new IntervalFileSniffer(p);
		
		if (sniffer.isBigBed()) {
			return new BigBedFile(p);
		} else if (sniffer.isBAM()) {
			return new SAMFile(p);
		} else if (sniffer.isBedGraph()) {
			return new BedGraphFile(p);
		} else if (sniffer.isBed()) {
			return new BedFile(p);
		} else if (sniffer.isSAM()) {
				return new SAMFile(p);
		} else {
			throw new IntervalFileSnifferException("Could not autodetect Interval file format");
		}
	}
	
	public Iterator<T> query(Interval i) throws UnsupportedOperationException {
		return query(i.getChr(), i.low(), i.high());
	}
	
	public abstract Iterator<T> query(String chr, int start, int stop) throws UnsupportedOperationException;
}

package edu.unc.genomics.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.unc.genomics.Interval;

/**
 * @author timpalpant
 *
 */
public abstract class IntervalFile<T extends Interval> implements Iterable<T>, Closeable {
	
	private static final Logger log = Logger.getLogger(IntervalFile.class);
	
	protected Path p;
	
	protected IntervalFile(Path p) {
		this.p = p;
	}
	
	public static IntervalFile<? extends Interval> autodetect(Path p) throws IntervalFileSnifferException, IOException {
		IntervalFileSniffer sniffer = new IntervalFileSniffer(p);
		
		if (sniffer.isBigBed()) {
			log.info("Autodetected BigBed filetype for: " + p.getFileName().toString());
			return new BigBedFile(p);
		} else if (sniffer.isBAM()) {
			log.info("Autodetected BAM filetype for: " + p.getFileName().toString());
			return new SAMFile(p);
		} else if (sniffer.isGFF()) {
			log.info("Autodetected GFF filetype for: " + p.getFileName().toString());
			return new GFFFile(p);
		} else if (sniffer.isBedGraph()) {
			log.info("Autodetected BedGraph filetype for: " + p.getFileName().toString());
			return new BedGraphFile(p);
		} else if (sniffer.isBed()) {
			log.info("Autodetected Bed filetype for: " + p.getFileName().toString());
			return new BedFile(p);
		} else if (sniffer.isSAM()) {
			log.info("Autodetected SAM filetype for: " + p.getFileName().toString());
				return new SAMFile(p);
		} else {
			throw new IntervalFileSnifferException("Could not autodetect Interval file format");
		}
	}
	
	public static List<Interval> loadAll(Path p) throws IntervalFileSnifferException, IOException {
		List<Interval> intervals = new ArrayList<Interval>();
		try (IntervalFile<? extends Interval> intervalFile = autodetect(p)) {
			for (Interval interval : intervalFile) {
				intervals.add(interval);
			}
		}
		
		return intervals;
	}
	
	public abstract int count();
	
	public abstract Set<String> chromosomes();
	
	public Iterator<T> query(Interval i) throws UnsupportedOperationException {
		return query(i.getChr(), i.low(), i.high());
	}
	
	public abstract Iterator<T> query(String chr, int start, int stop) throws UnsupportedOperationException;
	
	public Path getPath() {
		return p;
	}
}
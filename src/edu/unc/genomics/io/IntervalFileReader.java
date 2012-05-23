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
 * Base class for reading files of Interval information, including Bed, BigBed, BedGraph, GFF, GeneTrack, SAM, and BAM files
 * Subclasses will provide type-specific information, but this class may be used when only Interval info (chr:start-stop) is needed
 * 
 * @author timpalpant
 *
 */
public abstract class IntervalFileReader<T extends Interval> implements Iterable<T>, Closeable {
	
	private static final Logger log = Logger.getLogger(IntervalFileReader.class);
	
	protected Path p;
	
	protected IntervalFileReader(Path p) {
		this.p = p;
	}
	
	/**
	 * Attempt to autodetect file type and return the appropriate parser
	 * @param p the file to initialize
	 * @return an IntervalFile that will parse the data in p correctly
	 * @throws IntervalFileSnifferException if the file type cannot be autodetected (this may be thrown if the file has an invalid format)
	 * @throws IOException if a disk read error occurs
	 */
	public static IntervalFileReader<? extends Interval> autodetect(Path p) throws IntervalFileSnifferException, IOException {
		IntervalFileSniffer sniffer = new IntervalFileSniffer(p);
		
		if (sniffer.isBigBed()) {
			log.debug("Autodetected BigBed filetype for: " + p.getFileName().toString());
			return new BigBedFileReader(p);
		} else if (sniffer.isBAM()) {
			log.debug("Autodetected BAM filetype for: " + p.getFileName().toString());
			return new BAMFileReader(p);
		} else if (sniffer.isGFF()) {
			log.debug("Autodetected GFF filetype for: " + p.getFileName().toString());
			return new GFFFileReader(p);
		} else if (sniffer.isBedGraph()) {
			log.debug("Autodetected BedGraph filetype for: " + p.getFileName().toString());
			return new BedGraphFileReader(p);
		} else if (sniffer.isBed()) {
			log.debug("Autodetected Bed filetype for: " + p.getFileName().toString());
			return new BedFileReader(p);
		} else if (sniffer.isSAM()) {
			log.debug("Autodetected SAM filetype for: " + p.getFileName().toString());
			return new SAMFileReader(p);
		} else if (sniffer.isGeneTrack()) {
			log.debug("Autodetected GeneTrack filetype for: " + p.getFileName().toString());
			return new GeneTrackFileReader(p);
		} else {
			throw new IntervalFileSnifferException("Could not autodetect Interval file format");
		}
	}
	
	/**
	 * Load all of the intervals from a file. WARN: this may consume a lot of memory if the source file is large
	 * @param p the file to load intervals from
	 * @return a List of intervals from p
	 * @throws IntervalFileSnifferException
	 * @throws IOException
	 */
	public static List<Interval> loadAll(Path p) throws IntervalFileSnifferException, IOException {
		List<Interval> intervals = new ArrayList<Interval>();
		try (IntervalFileReader<? extends Interval> intervalFile = autodetect(p)) {
			for (Interval interval : intervalFile) {
				intervals.add(interval);
			}
		}
		
		return intervals;
	}
	
	/**
	 * @return all of the intervals in this IntervalFile
	 */
	public List<T> loadAll() {
		List<T> intervals = new ArrayList<>();
		for (T interval : this) {
			intervals.add(interval);
		}
		return intervals;
	}
	
	/**
	 * @return the number of intervals in this file
	 */
	public abstract int count();
	
	/**
	 * @return the set of all chromosomes that have intervals in this file
	 */
	public abstract Set<String> chromosomes();
	
	/**
	 * Query for intervals that overlap a given interval
	 * @param i the interval to query for
	 * @return an Iterator over the intervals in this file that overlap i
	 * @throws UnsupportedOperationException if the subclass does not support random queries
	 */
	public Iterator<T> query(Interval i) throws UnsupportedOperationException {
		return query(i.getChr(), i.low(), i.high());
	}
	
	/**
	 * Query for intervals that overlap a given interval
	 * @param chr the chromosome of the interval to query for
	 * @param start the start of the interval to query for
	 * @param stop the stop of the interval to query for
	 * @return an Iterator over intervals in this file that overlap chr:start-stop
	 * @throws UnsupportedOperationException if the subclass does not support random queries
	 */
	public abstract Iterator<T> query(String chr, int start, int stop) throws UnsupportedOperationException;
	
	public Path getPath() {
		return p;
	}
}

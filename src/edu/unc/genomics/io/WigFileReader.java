package edu.unc.genomics.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.ucsc.genome.TrackHeader;
import edu.unc.genomics.Contig;
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
public abstract class WigFileReader implements Closeable {
	
	private static final Logger log = Logger.getLogger(WigFileReader.class);
	protected final Path p;
	protected TrackHeader header = TrackHeader.newWiggle();
	
	protected WigFileReader(Path p) {
		this.p = p;
	}
	
	/**
	 * Autodetect whether a file is an ASCII-text Wig file or a BigWig file and initialize it
	 * @param p the file to initialize
	 * @return a WigFile handle to p that is the appropriate subclass (BigWigFile or TextWigFile)
	 * @throws IOException if an IO error occurs while trying to open the file
	 * @throws WigFileFormatException if the format cannot be autodetected, or there is an illegal format in the file, or the file is not actually a Wig file
	 */
	public static WigFileReader autodetect(Path p) throws IOException, WigFileFormatException {
		WigFileReader wig;
		
		if (BigWigFileReader.isBigWig(p)) {
			log.info("Autodetected BigWig file type: " + p);
			wig = new BigWigFileReader(p);
		} else {
			log.info("Autodetected Wiggle file type: " + p);
			wig = new TextWigFileReader(p);
		}
		
		return wig;
	}
	
	/**
	 * @return the path to this Wig file
	 */
	public final Path getPath() {
		return p;
	}
	
	/**
	 * @return the track header for this Wig file
	 */
	public final TrackHeader getHeader() {
		return header;
	}
	
	/**
	 * Query for a Contig of data in this Wig file corresponding to a specific interval
	 * @param interval the Interval of data to query for
	 * @return an Iterator of WigItems that overlap the Interval i
	 * @throws IOException if a disk read error occurs
	 * @throws WigFileException if the Wig file does not contain data for this Interval
	 */
	public abstract Contig query(Interval interval) throws IOException, WigFileException;
	
	/**
	 * Query for a Contig of data in this Wig file corresponding to a specific interval
	 * @param chr the chromosome of the interval
	 * @param start the start base pair of the interval
	 * @param stop the stop base pair of the interval
	 * @return an Iterator of WigItems that overlap the specified interval
	 * @throws IOException if a disk read error occurs
	 * @throws WigFileException if the Wig file does not contain data for this Interval
	 */
	public final Contig query(String chr, int start, int stop) throws IOException, WigFileException {
		return query(new Interval(chr, start, stop));
	}
	
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
	public final boolean includes(Interval i) {
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

package edu.unc.genomics.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author timpalpant
 * Holds information about a TextWigFile
 * and some kind of index for finding data
 */
class WigIndex {
	protected List<Contig> contigs;
	
	protected long numBases;
	protected double total;
	protected double mean;
	protected double stdev;
	protected double min;
	protected double max;
	
	public static WigIndex indexFile(Path p) throws IOException {
		BufferedReader reader = Files.newBufferedReader(p, Charset.defaultCharset());
		
		WigIndex index = new WigIndex();
		String line;
		while ((line = reader.readLine()) != null) {
			// TODO: do the indexing
		}
		
		return index;
	}
	
	/**
	 * @return the numBases
	 */
	public long getNumBases() {
		return numBases;
	}
	/**
	 * @param numBases the numBases to set
	 */
	public void setNumBases(long numBases) {
		this.numBases = numBases;
	}
	/**
	 * @return the total
	 */
	public double getTotal() {
		return total;
	}
	/**
	 * @param total the total to set
	 */
	public void setTotal(double total) {
		this.total = total;
	}
	/**
	 * @return the mean
	 */
	public double getMean() {
		return mean;
	}
	/**
	 * @param mean the mean to set
	 */
	public void setMean(double mean) {
		this.mean = mean;
	}
	/**
	 * @return the stdev
	 */
	public double getStdev() {
		return stdev;
	}
	/**
	 * @param stdev the stdev to set
	 */
	public void setStdev(double stdev) {
		this.stdev = stdev;
	}
	/**
	 * @return the min
	 */
	public double getMin() {
		return min;
	}
	/**
	 * @param min the min to set
	 */
	public void setMin(double min) {
		this.min = min;
	}
	/**
	 * @return the max
	 */
	public double getMax() {
		return max;
	}
	/**
	 * @param max the max to set
	 */
	public void setMax(double max) {
		this.max = max;
	}
	
	
	/**
	 * @author timpalpant
	 * Holds information about a Contig in a WigFile
	 */
	private static class Contig {
		
	}
}

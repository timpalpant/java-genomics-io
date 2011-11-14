package edu.unc.genomics.io;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import edu.ucsc.genome.TrackHeader;
import edu.unc.genomics.Contig;

public abstract class WigFile {
	protected TrackHeader header;
	protected Path p;
	protected WigIndex index;
	
	protected List<String> chromosomes;
	
	protected WigFile(Path p) {
		this.p = p;
		this.header = new TrackHeader("wiggle_0");
		this.index = new WigIndex();
	}
	
	public abstract Contig query(String chr, int start, int stop);
	
	public List<String> chromosomes() {
		if (chromosomes == null) {
			chromosomes = new ArrayList<String>();
			
		}
		
		return chromosomes;
	}
	
	public int getChrStart(String chr) {
		
	}
	
	public int getChrStop(String chr) {
		
	}
	
	public boolean includes(String chr, int start, int stop) {
		
	}
	
	public boolean includes(String chr, int start) {
		
	}
	
	public boolean includes(String chr) {
		
	}
	
	public long length() {
		return size();
	}
	
	public long size() {
		return numBases();
	}
	
	public long numBases() {
		return index.getNumBases();
	}
	
	public double total() {
		return index.getTotal();
	}
	
	public double mean() {
		return index.getMean();
	}
	
	public double stdev() {
		return index.getStdev();
	}
	
	public double min() {
		return index.getMin();
	}
	
	public double max() {
		return index.getMax();
	}
	
	public String toString() {
		
	}
}

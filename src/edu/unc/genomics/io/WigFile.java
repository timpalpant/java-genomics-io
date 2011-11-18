package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import edu.unc.genomics.Interval;

public abstract class WigFile {
	protected Path p;
	
	protected WigFile(Path p) {
		this.p = p;
	}
	
	public static WigFile autodetect(Path p) throws IOException, WigFileException {
		WigFile wig;
		
		if (BigWigFile.isBigWig(p)) {
			wig = new BigWigFile(p);
		} else {
			wig = new TextWigFile(p);
		}
		
		return wig;
	}
	
	public float[] query(Interval i) throws IOException, WigFileException {
		return query(i.getChr(), i.getStart(), i.getStop());
	}
	
	public abstract float[] query(String chr, int start, int stop) throws IOException, WigFileException;
	
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

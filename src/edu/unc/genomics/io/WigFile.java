package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class WigFile {
	protected Path p;
	
	protected WigFile(Path p) {
		this.p = p;
	}
	
	public static WigFile autodetect(Path p) throws IOException {
		WigFile wig;
		
		if (BigWigFile.isBigWig(p)) {
			wig = new BigWigFile(p);
		} else {
			wig = new TextWigFile(p);
		}
		
		return wig;
	}
	
	public abstract double[] query(String chr, int start, int stop);
	
	public abstract List<String> chromosomes();
	
	public abstract int getChrStart(String chr);
	
	public abstract int getChrStop(String chr);
	
	public abstract boolean includes(String chr, int start, int stop);
	
	public abstract boolean includes(String chr);
	
	public abstract long length();
	
	public abstract long numBases();
	
	public abstract double total();
	
	public abstract double mean();
	
	public abstract double stdev();
	
	public abstract double min();
	
	public abstract double max();
	
	public abstract String toString();
}

package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bbfile.BBTotalSummaryBlock;
import org.broad.igv.bbfile.BigWigIterator;
import org.broad.igv.bbfile.RPChromosomeRegion;
import org.broad.igv.bbfile.WigItem;

/**
 * Wrapper for Broad Institute BigWig classes
 * See: http://code.google.com/p/bigwig/
 * 
 * @author timpalpant
 *
 */
public class BigWigFile extends WigFile {
	private BBFileReader reader;
	private BBTotalSummaryBlock summary;

	public BigWigFile(Path p) throws IOException {
		super(p);
		
		reader = new BBFileReader(p.toString());
		summary = reader.getTotalSummaryBlock();
	}
	
	public static boolean isBigWig(Path p) throws IOException {
		boolean isBigWig = false;
		try {
			BBFileReader reader = new BBFileReader(p.toString());
			isBigWig = reader.isBigWigFile();
		} catch (RuntimeException e) { }
		return isBigWig;
	}
	
	@Override
	public float[] query(String chr, int start, int stop) throws WigFileException {
		if (!includes(chr, start, stop)) {
			throw new WigFileException("BigWigFile does not contain data for region: " + chr + ":" + start + "-" + stop);
		}
		
		int length = stop - start + 1;
		float[] result = new float[length];
		
		BigWigIterator it = reader.getBigWigIterator(chr, start, chr, stop, false);
		while (it.hasNext()) {
			WigItem item = it.next();
			for (int i = item.getStartBase(); i <= item.getEndBase(); i++) {
				result[i-start] = item.getWigValue();
			}
		}
		
		return result;
	}

	@Override
	public Set<String> chromosomes() {
		return new HashSet<String>(reader.getChromosomeNames());
	}

	@Override
	public int getChrStart(String chr) {
		int chrID = reader.getChromosomeID(chr);
		RPChromosomeRegion region = reader.getChromosomeBounds(chrID, chrID);
		return region.getStartBase();
	}

	@Override
	public int getChrStop(String chr) {
		int chrID = reader.getChromosomeID(chr);
		RPChromosomeRegion region = reader.getChromosomeBounds(chrID, chrID);
		return region.getEndBase();
	}

	@Override
	public boolean includes(String chr, int start, int stop) {
		int chrID = reader.getChromosomeID(chr);
		if (chrID == -1) { return false; }
		RPChromosomeRegion region = reader.getChromosomeBounds(chrID, chrID);
		return region.getStartBase() <= start && region.getEndBase() >= stop;
	}

	@Override
	public boolean includes(String chr) {
		return reader.getChromosomeID(chr) != -1;
	}

	@Override
	public long numBases() {
		return summary.getBasesCovered();
	}

	@Override
	public double total() {
		return summary.getSumData();
	}

	@Override
	public double mean() {
		return total() / numBases();
	}

	@Override
	public double stdev() {
		return Math.sqrt((summary.getSumSquares()-Math.pow(mean(), 2))/numBases());
	}

	@Override
	public double min() {
		return summary.getMinVal();
	}

	@Override
	public double max() {
		return summary.getMinVal();
	}

	@Override
	public String toString() {
		return summary.toString();
	}

}

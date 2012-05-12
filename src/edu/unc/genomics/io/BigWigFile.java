package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bbfile.BBTotalSummaryBlock;
import org.broad.igv.bbfile.BigWigIterator;
import org.broad.igv.bbfile.RPChromosomeRegion;
import org.broad.igv.bbfile.WigItem;

/**
 * A BigWig file. For more information, see: http://genome.ucsc.edu/goldenPath/help/bigWig.html
 * 
 * BigWig and regular ASCII-text Wig files may be used interchangeably as the base class WigFile,
 * and the correct format (Wig/BigWig) can be autodetected by calling WigFile.autodetect()
 * 
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
	public void close() { }
	
	@Override
	public Iterator<WigItem> query(String chr, int low, int high) throws WigFileException {
		if (!includes(chr, low, high)) {
			throw new WigFileException("BigWigFile does not contain data for region: " + chr + ":" + low + "-" + high);
		}
		
		return new BigWigCoordinateChangeIterator(reader.getBigWigIterator(chr, low-1, chr, high, false));
	}

	@Override
	public Set<String> chromosomes() {
		return new HashSet<String>(reader.getChromosomeNames());
	}

	@Override
	public int getChrStart(String chr) {
		int chrID = reader.getChromosomeID(chr);
		RPChromosomeRegion region = reader.getChromosomeBounds(chrID, chrID);
		return region.getStartBase()+1;
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
		return Math.sqrt(summary.getSumSquares()/numBases() - Math.pow(mean(), 2));
	}

	@Override
	public double min() {
		return summary.getMinVal();
	}

	@Override
	public double max() {
		return summary.getMaxVal();
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("BigWig file:\n");
		
		for (String chr : chromosomes()) {
			s.append("Chromosome ").append(chr).append(", start=").append(getChrStart(chr));
			s.append(", stop=").append(getChrStop(chr)).append("\n");
		}
		
		s.append("Basic Statistics:\n");
		s.append("\tMean:\t\t\t").append(mean()).append("\n");
		s.append("\tStandard Deviation:\t").append(stdev()).append("\n");
		s.append("\tTotal:\t\t\t").append(total()).append("\n");
		s.append("\tBases Covered:\t\t").append(numBases()).append("\n");
		s.append("\tMin value:\t\t").append(min()).append("\n");
		s.append("\tMax value:\t\t").append(max());
		
		return s.toString();
	}
	
	/**
	 * BigWig files are 0-indexed, half-open
	 * To conform with the rest of the java-genomics-io package, 
	 * wrap and shift the results to 1-indexed, closed
	 * @author timpalpant
	 *
	 */
	private static class BigWigCoordinateChangeIterator implements Iterator<WigItem> {

		private final BigWigIterator bwIter;
		
		private BigWigCoordinateChangeIterator(BigWigIterator bwIter) {
			this.bwIter = bwIter;
		}
		
		@Override
		public boolean hasNext() {
			return bwIter.hasNext();
		}

		@Override
		public WigItem next() {
			WigItem item = bwIter.next();
			WigItem shifted = new WigItem(item.getItemNumber(), item.getChromosome(), item.getStartBase()+1, item.getEndBase(), item.getWigValue());
			return shifted;
		}

		@Override
		public void remove() {
			bwIter.remove();
		}
	}

}

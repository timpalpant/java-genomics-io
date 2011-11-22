/**
 * 
 */
package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bbfile.BedFeature;

import edu.unc.genomics.BedEntry;

/**
 * @author timpalpant
 *
 */
public class BigBedFile extends IntervalFile<BedEntry> {

	BBFileReader reader;
	
	protected BigBedFile(Path p) throws IOException {
		super(p);
		reader = new BBFileReader(p.toString());
		if (!reader.isBigBedFile()) {
			throw new IntervalFileFormatException("Not a BigBed file!");
		}
	}

	@Override
	public Iterator<BedEntry> iterator() {
		return new BigBedEntryIterator(reader.getBigBedIterator());
	}

	@Override
	public void close() throws IOException { }

	@Override
	public Iterator<BedEntry> query(String chr, int start, int stop)
			throws UnsupportedOperationException {
		return new BigBedEntryIterator(reader.getBigBedIterator(chr, start, chr, stop, false));
	}
	
	/**
	 * Wrapper to convert BedFeatures to BedEntry
	 * 
	 * @author timpalpant
	 *
	 */
	private static class BigBedEntryIterator implements Iterator<BedEntry> {

		private final Iterator<BedFeature> it;
		
		public BigBedEntryIterator(Iterator<BedFeature> it) {
			this.it = it;
		}
		
		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public BedEntry next() {
			BedFeature f = it.next();
			BedEntry bed = new BedEntry(f.getChromosome(), f.getStartBase(), f.getEndBase());
			String[] fields = f.getRestOfFields();
			if (fields.length > 0) {
				bed.setId(fields[0]);
			}
			
			if (fields.length > 1) {
				bed.setValue(Double.valueOf(fields[1]));
			}
			
			// Reverse start/stop if on the - strand
			if (fields.length > 2 && fields[2].equalsIgnoreCase("-") && bed.isWatson()) {
				int tmp = bed.getStart();
				bed.setStart(bed.getStop());
				bed.setStop(tmp);
			}
			
			// TODO: Parse other BedFeature fields
			
			return bed;
		}

		@Override
		public void remove() {
			it.remove();
		}
	}

}

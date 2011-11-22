/**
 * 
 */
package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecordIterator;

import edu.unc.genomics.SAMEntry;

/**
 * @author timpalpant
 *
 */
public class SAMFile extends IntervalFile<SAMEntry> {
	
	private SAMFileReader reader;
	
	public SAMFile(Path p) {
		super(p);
		reader = new SAMFileReader(p.toFile());
	}
	
	@Override
	public void close() throws IOException {
		reader.close();
	}

	@Override
	public Iterator<SAMEntry> iterator() {
		return new SAMEntryIterator(reader.iterator());
	}

	@Override
	public Iterator<SAMEntry> query(String chr, int start, int stop) {
		return new SAMEntryIterator(reader.query(chr, start, stop, false));
	}
	
	/**
	 * @author timpalpant
	 * Wrapper around Picard's SAMRecordIterator to return SAMEntry's
	 */
	class SAMEntryIterator implements Iterator<SAMEntry> {

		private SAMRecordIterator it;
		
		public SAMEntryIterator(SAMRecordIterator it) {
			this.it = it;
		}
		
		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public SAMEntry next() {
			return new SAMEntry(it.next());
		}

		@Override
		public void remove() {
			it.remove();
		}
	}
}

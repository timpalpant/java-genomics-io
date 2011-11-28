/**
 * 
 */
package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sf.samtools.BAMIndex;
import net.sf.samtools.BAMIndexMetaData;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;

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
	public int count() {
		int count = 0;
		
		if (reader.hasIndex()) {
			BAMIndex index = reader.getIndex();
      int nRefs = reader.getFileHeader().getSequenceDictionary().size();
      for (int i = 0; i < nRefs; i++) {
      	BAMIndexMetaData data = index.getMetaData(i);
      	count += data.getAlignedRecordCount();
      	count += data.getUnalignedRecordCount();
      }
		} else {
			for (SAMRecord r : reader) {
				count++;
			}
		}
		
		return count;
	}

	@Override
	public Set<String> chromosomes() {
		Set<String> chromosomes = new HashSet<String>();
		// TODO: Test if this works with both SAM and BAM files
		SAMSequenceDictionary dict = reader.getFileHeader().getSequenceDictionary();
		for (SAMSequenceRecord seqRec : dict.getSequences()) {
			chromosomes.add(seqRec.getSequenceName());
		}
		return chromosomes;
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

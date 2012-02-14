package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import net.sf.samtools.BAMIndex;
import net.sf.samtools.BAMIndexMetaData;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;

import edu.unc.genomics.SAMEntry;
import edu.unc.genomics.util.Samtools;

/**
 * @author timpalpant
 *
 */
public class SAMFile extends IntervalFile<SAMEntry> {
	
	private static final Logger log = Logger.getLogger(SAMFile.class);
	
	private SAMFileReader reader;
	private Path index;
	private SAMRecordIterator it;
	
	public SAMFile(Path p) {
		super(p);
		reader = new SAMFileReader(p.toFile());
		
		// Automatically index BAM files that do not have an index
		if (reader.isBinary()) {
			if (!reader.hasIndex()) {
				log.debug("Generating index for BAM file: " + p);
				index = p.resolveSibling(p.getFileName()+".bai");
				Samtools.indexBAMFile(reader, index);
				// Now that we have an index, reset the reader
				reader = new SAMFileReader(p.toFile());
			} else {
				index = Samtools.findIndexFile(p);
			}
		}
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
		// Close any previous iterators since SAM-JDK only allows one at a time
		if (it != null) {
			it.close();
		}

		it = reader.iterator();
		return new SAMEntryIterator();
	}

	@Override
	public Iterator<SAMEntry> query(String chr, int start, int stop) {
		// Close any previous iterators since SAM-JDK only allows one at a time
		if (it != null) {
			it.close();
		}

		it = reader.query(chr, start, stop, false);
		return new SAMEntryIterator();
	}
	
	/**
	 * @author timpalpant
	 * Wrapper around Picard's SAMRecordIterator to return SAMEntry's
	 */
	private class SAMEntryIterator implements Iterator<SAMEntry> {
		
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

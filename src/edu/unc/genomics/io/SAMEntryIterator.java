package edu.unc.genomics.io;

import java.util.Iterator;

import net.sf.samtools.SAMRecordIterator;

import edu.unc.genomics.SAMEntry;

/**
 * @author timpalpant
 * Wrapper around Picard's SAMRecordIterator to return SAMEntry's
 */
class SAMEntryIterator implements Iterator<SAMEntry> {
	
	private final SAMRecordIterator it;
	
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

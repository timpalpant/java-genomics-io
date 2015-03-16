package edu.unc.genomics.io;

import java.util.Iterator;

import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

import edu.unc.genomics.SAMEntry;

/**
 * @author timpalpant Wrapper around Picard's SAMRecordIterator to return
 *         SAMEntry's
 */
class SAMEntryIterator implements Iterator<SAMEntry> {

  private final SAMRecordIterator it;
  private final boolean allowUnmapped;
  private SAMRecord nextRecord;

  public SAMEntryIterator(SAMRecordIterator it, boolean allowUnmapped) {
    this.it = it;
    this.allowUnmapped = allowUnmapped;
    advance();
  }

  @Override
  public boolean hasNext() {
    return nextRecord != null;
  }

  @Override
  public SAMEntry next() {
    SAMEntry entry = new SAMEntry(nextRecord);
    advance();
    return entry;
  }

  @Override
  public void remove() {
    it.remove();
  }

  private void advance() {
    nextRecord = null;

    if (it.hasNext()) {
      nextRecord = it.next();
      // Find the next mapped read
      if (!allowUnmapped) {
        while (it.hasNext() && nextRecord.getReadUnmappedFlag()) {
          nextRecord = it.next();
        }
      }
    }
  }
}

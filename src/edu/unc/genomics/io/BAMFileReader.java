package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import net.sf.samtools.BAMIndex;
import net.sf.samtools.BAMIndexMetaData;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;

import edu.unc.genomics.SAMEntry;
import edu.unc.genomics.util.Samtools;

/**
 * A binary BAM file. Will passively index if an index is not available and
 * random queries are attempted.
 * 
 * For more information about BAM files, see: http://samtools.sourceforge.net
 * 
 * @author timpalpant
 *
 */
public class BAMFileReader extends IntervalFileReader<SAMEntry> {

  private static final Logger log = Logger.getLogger(BAMFileReader.class);

  private Set<String> chromosomes;
  private SAMFileReader reader;
  private Path index;
  private SAMRecordIterator it;

  /**
   * By default, consider all alignments (mapped or unmapped)
   */
  private boolean allowUnmappedReads = true;

  public BAMFileReader(Path p) {
    super(p);

    // Automatically index BAM files that do not have an index
    log.debug("Opening BAM file reader " + p);
    reader = new SAMFileReader(p.toFile());
    if (!reader.hasIndex()) {
      try {
        index = Files.createTempFile(p.getFileName().toString(), BAMIndex.BAMIndexSuffix);
      } catch (IOException e) {
        log.error("Error creating temporary BAM index for: " + p.getFileName());
        e.printStackTrace();
        throw new RuntimeException("Error creating temporary BAM index for: " + p.getFileName());
      }

      // Hook for automatically deleting the BAM index when the JVM terminates
      index.toFile().deleteOnExit();

      // Create the index
      Samtools.indexBAMFile(p, index);

      // Now that we have an index, reset the reader
      reader = new SAMFileReader(p.toFile(), index.toFile());
      // and ensure that we now have an index
      if (!reader.hasIndex()) {
        throw new IntervalFileFormatException("Error indexing BAM file: " + p);
      }
    }

    // Turn off memory mapping to avoid BufferUnderRun exceptions
    reader.enableIndexMemoryMapping(false);
    // Turn on index caching
    reader.enableIndexCaching(true);
  }

  public BAMFileReader(Path p, boolean allowUnmappedReads) {
    this(p);
    this.allowUnmappedReads = allowUnmappedReads;
  }

  @Override
  public void close() throws IOException {
    log.debug("Closing BAM file " + p);
    reader.close();
  }

  @Override
  public int count() {
    int count = 0;
    BAMIndex index = reader.getIndex();
    int nRefs = reader.getFileHeader().getSequenceDictionary().size();
    for (int i = 0; i < nRefs; i++) {
      BAMIndexMetaData data = index.getMetaData(i);
      count += data.getAlignedRecordCount();
      count += data.getUnalignedRecordCount();
    }
    return count;
  }

  @Override
  public Set<String> chromosomes() {
    if (chromosomes == null) {
      chromosomes = new LinkedHashSet<String>();
      SAMSequenceDictionary dict = reader.getFileHeader().getSequenceDictionary();
      for (SAMSequenceRecord seqRec : dict.getSequences()) {
        chromosomes.add(seqRec.getSequenceName());
      }
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
    return new SAMEntryIterator(it, allowUnmappedReads);
  }

  @Override
  public Iterator<SAMEntry> query(String chr, int start, int stop) {
    // Close any previous iterators since SAM-JDK only allows one at a time
    if (it != null) {
      it.close();
    }

    it = reader.query(chr, start, stop, false);
    return new SAMEntryIterator(it, allowUnmappedReads);
  }

  /**
   * @return the allowUnmappedReads
   */
  public boolean doesAllowUnmappedReads() {
    return allowUnmappedReads;
  }

  /**
   * @param allowUnmappedReads
   *          the allowUnmappedReads to set
   */
  public void setAllowUnmappedReads(boolean allowUnmappedReads) {
    this.allowUnmappedReads = allowUnmappedReads;
  }

}

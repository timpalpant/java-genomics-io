package edu.unc.genomics.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.log4j.Logger;

import edu.unc.genomics.SAMEntry;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileWriterFactory;

/**
 * A class for writing BAM files to disk
 * 
 * @author timpalpant
 *
 */
public class BAMFileWriter implements Closeable {

  private static final Logger log = Logger.getLogger(BAMFileWriter.class);

  private final Path p;
  private final net.sf.samtools.SAMFileWriter writer;
  private final SAMFileHeader header = new SAMFileHeader();

  public BAMFileWriter(Path p) {
    this.p = p;
    log.debug("Opening BAM file writer " + p);
    this.writer = new SAMFileWriterFactory().makeBAMWriter(header, false, p.toFile());
  }

  @Override
  public void close() throws IOException {
    log.debug("Closing BAM file writer " + p);
    writer.close();
  }

  public void write(SAMEntry entry) {
    synchronized (writer) {
      writer.addAlignment(entry.getSAMRecord());
    }
  }

  /**
   * @return the path to this file
   */
  public Path getPath() {
    return p;
  }
}

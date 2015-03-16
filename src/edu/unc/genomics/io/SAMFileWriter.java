package edu.unc.genomics.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.log4j.Logger;

import edu.unc.genomics.SAMEntry;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileWriterFactory;

public class SAMFileWriter implements Closeable {

  private static final Logger log = Logger.getLogger(SAMFileWriter.class);

  private final Path p;
  private final net.sf.samtools.SAMFileWriter writer;
  private final SAMFileHeader header = new SAMFileHeader();

  public SAMFileWriter(Path p) {
    this.p = p;
    log.debug("Opening SAM file writer " + p);
    this.writer = new SAMFileWriterFactory().makeSAMWriter(header, false, p.toFile());
  }

  @Override
  public void close() throws IOException {
    log.debug("Closing SAM file writer " + p);
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

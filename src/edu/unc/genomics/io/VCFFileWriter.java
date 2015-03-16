package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import org.apache.log4j.Logger;

import edu.unc.genomics.VCFEntry;

/**
 * A class for writing VCF files to disk
 * 
 * @author timpalpant
 *
 */
public class VCFFileWriter extends IntervalFileWriter<VCFEntry> {

  private static final Logger log = Logger.getLogger(VCFFileWriter.class);

  public VCFFileWriter(Path p, OpenOption... options) throws IOException {
    super(p, options);
    log.debug("Opening VCF file writer " + p);
  }

  @Override
  public void write(VCFEntry entry) {
    write(entry.toVCF());
  }

}

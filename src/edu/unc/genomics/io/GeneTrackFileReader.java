package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.log4j.Logger;

import net.sf.samtools.TabixWriter;
import net.sf.samtools.TabixWriter.Conf;

import edu.unc.genomics.GeneTrackEntry;
import edu.unc.genomics.IntervalFactory;

/**
 * A GeneTrack formatted file. For more information, see:
 * http://atlas.bx.psu.edu/genetrack/docs/genetrack.html
 * 
 * @author timpalpant
 *
 */
public class GeneTrackFileReader extends TextIntervalFileReader<GeneTrackEntry> {

  private static final Logger log = Logger.getLogger(GeneTrackFileReader.class);

  public GeneTrackFileReader(Path p) throws IOException {
    super(p, new GeneTrackEntryFactory());
    log.debug("Opening GeneTrack file reader " + p);
  }

  public static class GeneTrackEntryFactory implements IntervalFactory<GeneTrackEntry> {

    @Override
    public GeneTrackEntry parse(String line) {
      return GeneTrackEntry.parse(line);
    }

    @Override
    public Conf tabixConf() {
      return new Conf(TabixWriter.TI_FLAG_UCSC, 1, 2, 2, '#', 0);
    }

  }

}

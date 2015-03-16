package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import net.sf.samtools.TabixReader;

import edu.unc.genomics.Interval;
import edu.unc.genomics.IntervalFactory;

/**
 * For reading from Tabix-indexed files
 * 
 * @author timpalpant
 *
 */
public class TabixFileReader<T extends Interval> extends IntervalFileReader<T> {

  private static final Logger log = Logger.getLogger(TabixFileReader.class);

  private TabixReader reader;
  private IntervalFactory<T> factory;
  private int count = 0;

  protected TabixFileReader(Path p, IntervalFactory<T> factory) throws IOException {
    super(p);
    this.factory = factory;
    log.debug("Opening Tabix file reader " + p);
    reader = new TabixReader(p);
  }

  @Override
  public Iterator<T> iterator() {
    return new StringIntervalIterator<T>(reader.iterator(), factory);
  }

  @Override
  public void close() throws IOException {
    log.debug("Closing Tabix file reader " + p);
  }

  @Override
  public Iterator<T> query(String chr, int start, int stop) {
    return new StringIntervalIterator<T>(reader.query(chr, start, stop), factory);
  }

  @Override
  public int count() throws IntervalFileFormatException {
    // FIXME More efficiently count Tabix entries using index information?
    if (count == 0) {
      try {
        TabixReader tmpReader = new TabixReader(p);
        StringIntervalIterator<T> tmpIter = new StringIntervalIterator<T>(tmpReader.iterator(), factory);
        while (tmpIter.hasNext()) {
          tmpIter.next();
          count++;
        }
      } catch (IOException e) {
        log.error("Error counting entries in Tabix file " + p);
        e.printStackTrace();
        throw new IntervalFileFormatException("Error counting entries in Tabix file " + p);
      }
    }

    return count;
  }

  @Override
  public Set<String> chromosomes() {
    return reader.chromosomes();
  }

}

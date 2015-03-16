package net.sf.samtools;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class TabixReaderTest {

  private static final Path TEST_TABIX = Paths.get("test/fixtures/test.tabix.bed.gz");
  private TabixReader reader;

  @Before
  public void setUp() throws Exception {
    reader = new TabixReader(TEST_TABIX);
  }

  @Test
  public void testChromosomes() {
    Set<String> chromosomes = reader.chromosomes();
    assertEquals(4, chromosomes.size());
  }

  @Test
  public void testIterator() {
    int count = 0;
    for (String line : reader) {
      count++;
    }
    assertEquals(10, count);
  }

  @Test
  public void testQuery() {
    Iterator<String> it = reader.query("chrI", 5, 97);
    int count = 0;
    while (it.hasNext()) {
      it.next();
      count++;
    }
    assertEquals(3, count);
  }

}

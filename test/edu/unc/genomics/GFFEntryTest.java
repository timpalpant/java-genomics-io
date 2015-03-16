package edu.unc.genomics;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.unc.genomics.GFFEntry;
import edu.unc.genomics.Strand;

public class GFFEntryTest {

  private final String TEST_GFF_ENTRY = "chrIV\tSpotArray\tfeature\t11\t30\t10.0\t-\t.\tprobe_id=Spot1;count=1";
  private GFFEntry test;

  @Before
  public void setUp() throws Exception {
    test = GFFEntry.parse(TEST_GFF_ENTRY);
  }

  @Test
  public void testParse() {
    assertEquals("chrIV", test.getChr());
    assertEquals(30, test.getStart());
    assertEquals(11, test.getStop());
    assertEquals("Spot1", test.getId());
    assertEquals(10.0, test.getValue().doubleValue(), 1e-15);
    assertEquals(Strand.CRICK, test.strand());
  }

  @Test
  public void testOutput() {
    assertEquals(TEST_GFF_ENTRY, test.toOutput());
  }

}

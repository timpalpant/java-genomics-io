package edu.unc.genomics;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import edu.unc.genomics.Strand;

public class VCFEntryTest {

  private final String TEST_VCF_ENTRY = "20\t14370\trs6054257\tG\tA\t29.3\tPASS\tNS=3;DP=14;AF=0.5;DB;H2\tGT:GQ:DP:HQ\t0|0:48:1:51,51\t1|0:48:8:51,51\t1/1:43:5:.,.";
  private final String TEST_VCF_ENTRY_NO_QUAL = "20\t14370\trs6054257\tG\t.\t.\tPASS\tNS=3;DP=14;AF=0.5;DB;H2\tGT:GQ:DP:HQ\t0|0:48:1:51,51\t1|0:48:8:51,51\t1/1:43:5:.,.";

  @Test
  public void testParse() {
    VCFEntry test = VCFEntry.parse(TEST_VCF_ENTRY);
    assertEquals("20", test.getChr());
    assertEquals(14370, test.getStart());
    assertEquals(14370, test.getStop());
    assertEquals("rs6054257", test.getId());
    assertEquals(Strand.WATSON, test.strand());
    assertEquals("G", test.getRef());
    assertEquals(1, test.getAlt().length);
    assertEquals("A", test.getAlt()[0]);
    assertEquals(29.3, test.getQual(), 1e-15);
    assertEquals("PASS", test.getFilter());
    assertEquals("NS=3;DP=14;AF=0.5;DB;H2", test.getInfoString());
    assertEquals("GT:GQ:DP:HQ", test.getFormatString());
    assertEquals(3, test.getGenotypes().size());
    assertEquals("0|0:48:1:51,51", StringUtils.join(test.getGenotypes().get(0), ':'));
    assertEquals("1|0:48:8:51,51", StringUtils.join(test.getGenotypes().get(1), ':'));
    assertEquals("1/1:43:5:.,.", StringUtils.join(test.getGenotypes().get(2), ':'));
  }

  @Test
  public void testParseNoQual() {
    VCFEntry test = VCFEntry.parse(TEST_VCF_ENTRY_NO_QUAL);
    assertEquals("20", test.getChr());
    assertEquals(14370, test.getStart());
    assertEquals(14370, test.getStop());
    assertEquals("rs6054257", test.getId());
    assertEquals(Strand.WATSON, test.strand());
    assertEquals("G", test.getRef());
    assertEquals(null, test.getAlt());
    assertEquals(null, test.getQual());
    assertEquals("PASS", test.getFilter());
    assertEquals("NS=3;DP=14;AF=0.5;DB;H2", test.getInfoString());
    assertEquals("GT:GQ:DP:HQ", test.getFormatString());
    assertEquals(3, test.getGenotypes().size());
    assertEquals("0|0:48:1:51,51", StringUtils.join(test.getGenotypes().get(0), ':'));
    assertEquals("1|0:48:8:51,51", StringUtils.join(test.getGenotypes().get(1), ':'));
    assertEquals("1/1:43:5:.,.", StringUtils.join(test.getGenotypes().get(2), ':'));
  }

  @Test
  public void testOutput() {
    VCFEntry test = VCFEntry.parse(TEST_VCF_ENTRY);
    assertEquals(TEST_VCF_ENTRY, test.toOutput());
  }

}

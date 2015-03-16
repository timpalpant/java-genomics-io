package edu.unc.genomics.io;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

import edu.unc.genomics.Contig;

public abstract class AbstractWigFileReaderTest {

  protected WigFileReader test;

  @After
  public void tearDown() throws Exception {
    if (test != null) {
      test.close();
    }
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    // Delete the text wig file index
    Files.deleteIfExists(TextWigFileReaderTest.TEST_WIG.resolveSibling(TextWigFileReaderTest.TEST_WIG.getFileName()
        + TextWigFileReader.INDEX_EXTENSION));
  }

  @Test
  public void testAutodetect() throws WigFileException, IOException {
    WigFileReader text = WigFileReader.autodetect(TextWigFileReaderTest.TEST_WIG);
    assertTrue(text instanceof TextWigFileReader);
    text.close();

    WigFileReader bw = WigFileReader.autodetect(BigWigFileReaderTest.TEST_BIGWIG);
    assertTrue(bw instanceof BigWigFileReader);
    bw.close();
  }

  @Test
  public void testQueryFixedStep() throws WigFileException, IOException {
    Contig result = test.query("chrI", 5, 8);
    float[] data = result.getValues();
    assertEquals(4, data.length);
    float[] expected = { 5.0f, 6.0f, 7.0f, 8.0f };
    assertArrayEquals(expected, data, 1e-7f);
  }

  @Test
  public void testQueryVariableStep() throws WigFileException, IOException {
    Contig result = test.query("2micron", 101, 106);
    float[] data = result.getValues();
    assertEquals(6, data.length);
    float[] expected = { 6.0f, Float.NaN, Float.NaN, Float.NaN, 10.0f, Float.NaN };
    assertArrayEquals(expected, data, 1e-7f);
  }

  @Test
  public void testQueryCrick() throws WigFileException, IOException {
    Contig result = test.query("chrI", 8, 5);
    float[] data = result.getValues();
    assertEquals(4, data.length);
    float[] expected = { 8.0f, 7.0f, 6.0f, 5.0f };
    assertArrayEquals(expected, data, 1e-7f);
  }

  @Test
  public void testGetRange() throws WigFileException, IOException {
    Contig result = test.query("chrI", 5, 8);
    float[] subset = result.get(6, 7);
    assertEquals(2, subset.length);
    float[] expected = { 6.0f, 7.0f };
    assertArrayEquals(expected, subset, 1e-7f);
  }

  @Test
  public void testGetRangeCrick() throws WigFileException, IOException {
    Contig result = test.query("chrI", 5, 8);
    float[] subset = result.get(7, 6);
    assertEquals(2, subset.length);
    float[] expected = { 7.0f, 6.0f };
    assertArrayEquals(expected, subset, 1e-7f);
  }

  @Test
  public void testGetRangeDoubleCrick() throws WigFileException, IOException {
    Contig result = test.query("chrI", 8, 5);
    float[] subset = result.get(6, 7);
    assertEquals(2, subset.length);
    float[] expected = { 6.0f, 7.0f };
    assertArrayEquals(expected, subset, 1e-7f);
  }

  @Test
  public void testGetRangeOutside() throws WigFileException, IOException {
    Contig result = test.query("chrI", 5, 8);
    float[] data = result.get(4, 7);
    float[] expected = { Float.NaN, 5.0f, 6.0f, 7.0f };
    assertArrayEquals(expected, data, 1e-7f);
  }

  @Test
  public void testGet() throws WigFileException, IOException {
    Contig result = test.query("chrI", 5, 8);
    assertEquals(5.0f, result.get(5), 1e-7f);
    assertEquals(6.0f, result.get(6), 1e-7f);
    assertEquals(7.0f, result.get(7), 1e-7f);
    assertEquals(8.0f, result.get(8), 1e-7f);

    assertEquals(Float.NaN, result.get(4), 1e-7f);
    assertEquals(Float.NaN, result.get(9), 1e-7f);
    assertEquals(Float.NaN, result.get(-1), 1e-7f);
  }

  @Test
  public void testQueryStats() throws WigFileException, IOException {
    SummaryStatistics stats = test.queryStats("chrI", 5, 8);
    assertEquals(6.5, stats.getMean(), 1e-7);
    assertEquals(1.1180340051651, Math.sqrt(stats.getPopulationVariance()), 1e-7);
    assertEquals(5, stats.getMin(), 1e-7);
    assertEquals(8, stats.getMax(), 1e-7);
    assertEquals(4, stats.getN());
  }

  @Test
  public void testMeanQuery() throws WigFileException, IOException {
    Contig result = test.query("chrI", 5, 8);
    assertEquals(6.5, result.mean(), 1e-7);
  }

  /**
   * Test method for
   * {@link edu.unc.genomics.io.WigFileReader#stdev(java.util.Iterator)}.
   * 
   * @throws IOException
   * @throws WigFileException
   */
  @Test
  public void testStdevQuery() throws WigFileException, IOException {
    Contig result = test.query("chrI", 5, 8);
    assertEquals(1.1180340051651, result.stdev(), 1e-7);
  }

  /**
   * Test method for
   * {@link edu.unc.genomics.io.WigFileReader#min(java.util.Iterator)}.
   * 
   * @throws IOException
   * @throws WigFileException
   */
  @Test
  public void testMinQuery() throws WigFileException, IOException {
    Contig result = test.query("chrI", 5, 8);
    assertEquals(5, result.min(), 1e-7);
  }

  /**
   * Test method for
   * {@link edu.unc.genomics.io.WigFileReader#max(java.util.Iterator)}.
   * 
   * @throws IOException
   * @throws WigFileException
   */
  @Test
  public void testMaxQuery() throws WigFileException, IOException {
    Contig result = test.query("chrI", 5, 8);
    assertEquals(8, result.max(), 1e-7);
  }

  @Test
  public void testCoverageQuery() throws WigFileException, IOException {
    Contig result = test.query("chrI", 5, 8);
    assertEquals(4, result.coverage());
    assertEquals(4, result.numBases());
  }

  @Test()
  public void testQueryOutsideData() throws WigFileException, IOException {
    // API change 10/11/13: Readers no longer throw an exception
    // if data is outside the end of the chromosome; instead just
    // return NaNs where there is no data
    Contig result = test.query("chrI", -2, 8);
    assertEquals(8, result.coverage());
  }

  @Test
  public void testChromosomes() {
    assertEquals(3, test.chromosomes().size());
    assertTrue(test.chromosomes().contains("chrXI"));
    assertTrue(test.chromosomes().contains("chrI"));
    assertTrue(test.chromosomes().contains("2micron"));
  }

  @Test
  public void testGetChrStart() {
    assertEquals(20, test.getChrStart("chrXI"));
    assertEquals(1, test.getChrStart("chrI"));
    assertEquals(100, test.getChrStart("2micron"));
  }

  @Test
  public void testGetChrStop() {
    assertEquals(148, test.getChrStop("chrXI"));
    assertEquals(15, test.getChrStop("chrI"));
    assertEquals(111, test.getChrStop("2micron"));
  }

  @Test
  public void testIncludes() {
    assertFalse(test.includes("chrXX", 10, 10));
    assertTrue(test.includes("chrI", 1, 15));
    assertTrue(test.includes("2micron", 100, 104));
  }

  @Test
  public void testIncludesString() {
    assertTrue(test.includes("chrXI"));
    assertTrue(test.includes("chrI"));
    assertTrue(test.includes("2micron"));
    assertFalse(test.includes("chr22"));
  }

  @Test
  public void testNumBases() {
    assertEquals(124, test.numBases());
  }

  @Test
  public void testTotal() {
    assertEquals(952, test.total(), 1e-8);
  }

  @Test
  public void testMean() {
    assertEquals(7.67741935483871, test.mean(), 1e-8);
  }

  @Test
  public void testStdev() {
    assertEquals(8.413265216935388, test.stdev(), 1e-6);
  }

  @Test
  public void testMin() {
    assertEquals(0, test.min(), 1e-8);
  }

  @Test
  public void testMax() {
    assertEquals(44, test.max(), 1e-8);
  }

}

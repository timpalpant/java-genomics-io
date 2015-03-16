package edu.unc.genomics;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class FixedStepContigTest {

  private Contig test;

  @Before
  public void setUp() throws Exception {
    Interval interval = new Interval("chrV", 10, 19);
    float[] values = { Float.NaN, Float.NaN, 3.0f, 3.0f, Float.NaN, 3.0f, 3.0f, Float.NaN, 4.0f, 4.0f };
    test = new Contig(interval, values);
  }

  @Test
  public void testGetValues() {
    float[] expected = { Float.NaN, Float.NaN, 3.0f, 3.0f, Float.NaN, 3.0f, 3.0f, Float.NaN, 4.0f, 4.0f };
    assertArrayEquals(expected, test.getValues(), 1e-7f);
  }

  @Test
  public void testGetInterval() {
    Interval query = new Interval("chrIV", 11, 14);
    float[] result = test.get(query);
    assertNull(result);

    query = new Interval("chrV", 8, 12);
    result = test.get(query);
    float[] expected = { Float.NaN, Float.NaN, Float.NaN, Float.NaN, 3.0f };
    assertArrayEquals(expected, result, 1e-7f);

    query = new Interval("chrV", 18, 16);
    result = test.get(query);
    float[] expected2 = { 4.0f, Float.NaN, 3.0f };
    assertArrayEquals(expected2, result, 1e-7f);
  }

  @Test
  public void testGetRange() {
    float[] result = test.get(18, 16);
    float[] expected = { 4.0f, Float.NaN, 3.0f };
    assertArrayEquals(expected, result, 1e-7f);
  }

  @Test
  public void testGetValue() {
    assertEquals(Float.NaN, test.get(9), 1e-7f);
    assertEquals(4.0f, test.get(18), 1e-7f);
  }

  @Test
  public void testSetValuedInterval() {
    ValuedInterval interval = new ValuedInterval("chrV", 10, 11, "id", 2.0f);
    test.set(interval);
    float[] expected = { 2.0f, 2.0f, 3.0f, 3.0f, Float.NaN, 3.0f, 3.0f, Float.NaN, 4.0f, 4.0f };
    assertArrayEquals(expected, test.getValues(), 1e-7f);
  }

  @Test
  public void testSetInterval() {
    Interval interval = new Interval("chrV", 13, 11);
    test.set(interval, 2.0f);
    float[] expected = { Float.NaN, 2.0f, 2.0f, 2.0f, Float.NaN, 3.0f, 3.0f, Float.NaN, 4.0f, 4.0f };
    assertArrayEquals(expected, test.getValues(), 1e-7f);
  }

  @Test
  public void testSetRange() {
    test.set(12, 13, 2.0f);
    float[] expected = { Float.NaN, Float.NaN, 2.0f, 2.0f, Float.NaN, 3.0f, 3.0f, Float.NaN, 4.0f, 4.0f };
    assertArrayEquals(expected, test.getValues(), 1e-7f);
  }

  @Test
  public void testSetValue() {
    test.set(10, 2.0f);
    float[] expected = { 2.0f, Float.NaN, 3.0f, 3.0f, Float.NaN, 3.0f, 3.0f, Float.NaN, 4.0f, 4.0f };
    assertArrayEquals(expected, test.getValues(), 1e-7f);
  }

  @Test(expected = ContigException.class)
  public void testSetIllegal() {
    test.set(9, 1.0f);
  }

  @Test
  public void testCoverage() {
    assertEquals(6, test.coverage());
  }

  @Test
  public void testNumBases() {
    assertEquals(6, test.numBases());
  }

  @Test
  public void testTotal() {
    assertEquals(20.0f, test.total(), 1e-7f);
  }

  @Test
  public void testMean() {
    assertEquals(3.33333333f, test.mean(), 1e-7f);
  }

  @Test
  public void testStdev() {
    assertEquals(0.471404522f, test.stdev(), 1e-7f);
  }

  @Test
  public void testMin() {
    assertEquals(3.0f, test.min(), 1e-7f);
  }

  @Test
  public void testMax() {
    assertEquals(4.0f, test.max(), 1e-7f);
  }

  @Test
  public void testGetFixedStepHeader() {
    assertEquals("fixedStep chrom=chrV start=12 span=2 step=3", test.getFixedStepHeader());
  }

  @Test
  public void testGetVariableStepHeader() {
    assertEquals("variableStep chrom=chrV span=2", test.getVariableStepHeader());
  }

  @Test
  public void testGetFirstBaseWithData() {
    assertEquals(12, test.getFirstBaseWithData());
  }

  @Test
  public void testGetMinSpan() {
    assertEquals(2, test.getMinSpan());
  }

  @Test
  public void testGetVariableStepSpan() {
    assertEquals(2, test.getVariableStepSpan());
  }

  @Test
  public void testGetMinStep() {
    assertEquals(3, test.getMinStep());
  }

  @Test
  public void testIsFixedStep() {
    assertTrue(test.isFixedStep());
  }
}

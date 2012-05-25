package edu.unc.genomics;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class VariableStepContigTest {

	private Contig test;

	@Before
	public void setUp() throws Exception {
		Interval interval = new Interval("chrV", 10, 19);
		float[] values = {Float.NaN, Float.NaN, 3.0f, 3.0f, Float.NaN, 3.0f, 3.0f, 4.0f, 4.0f, Float.NaN};
		test = new Contig(interval, values);
	}
	
	@Test
	public void testGetValues() {
		float[] expected = {Float.NaN, Float.NaN, 3.0f, 3.0f, Float.NaN, 3.0f, 3.0f, 4.0f, 4.0f, Float.NaN};
		assertArrayEquals(expected, test.getValues(), 1e-7f);
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
		assertEquals("fixedStep chrom=chrV start=12 span=1 step=1", test.getFixedStepHeader());
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
	public void testGetMinStep() {
		assertEquals(1, test.getMinStep());
	}

	@Test
	public void testIsFixedStep() {
		assertFalse(test.isFixedStep());
	}

}

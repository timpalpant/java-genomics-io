package edu.genomics;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.unc.genomics.BedGraphEntry;

public class BedGraphEntryTest {

	private final String TEST_BEDGRAPH_ENTRY = "chrIV\t0\t10\t1.0";
	private BedGraphEntry test;

	@Before
	public void setUp() throws Exception {
		test = BedGraphEntry.parse(TEST_BEDGRAPH_ENTRY);
	}

	@Test
	public void testParse() {
		assertEquals("chrIV", test.getChr());
		assertEquals(1, test.getStart());
		assertEquals(10, test.getStop());
		assertEquals(1.0, test.getValue().doubleValue(), 1e-15);
	}
	
	@Test
	public void testOutput() {
		assertEquals(TEST_BEDGRAPH_ENTRY, test.toOutput());
	}

}

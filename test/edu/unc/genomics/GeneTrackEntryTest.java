package edu.unc.genomics;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.unc.genomics.GeneTrackEntry;

public class GeneTrackEntryTest {
	
	private final String TEST_GENETRACK_ENTRY = "chrIV\t100\t13.0\t1.0";
	private GeneTrackEntry test;

	@Before
	public void setUp() throws Exception {
		test = GeneTrackEntry.parse(TEST_GENETRACK_ENTRY);
	}

	@Test
	public void testParse() {
		assertEquals("chrIV", test.getChr());
		assertEquals(100, test.getStart());
		assertEquals(100, test.getStop());
		assertEquals(14.0, test.getValue().doubleValue(), 1e-15);
		assertEquals(13.0, test.getForward(), 1e-15);
		assertEquals(1.0, test.getReverse(), 1e-15);
	}

}

package edu.unc.genomics.io;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;

import org.broad.igv.bbfile.WigItem;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

public abstract class AbstractWigFileTest {
	
	protected WigFile test;
	
	@After
	public void tearDown() throws Exception {
		if (test != null) {
			test.close();
		}
	}
	
	@AfterClass
	public static void cleanUp() throws Exception {
		// Delete the text wig file index
		Files.deleteIfExists(TextWigFileTest.TEST_WIG.resolveSibling(TextWigFileTest.TEST_WIG.getFileName()+TextWigFile.INDEX_EXTENSION));
	}

	@Test
	public void testAutodetect() throws WigFileException, IOException {
		WigFile text = WigFile.autodetect(TextWigFileTest.TEST_WIG);
		text.close();
		
		WigFile bw = WigFile.autodetect(BigWigFileTest.TEST_BIGWIG);
		bw.close();
	}

	@Test
	public void testFlattenData() throws WigFileException, IOException {
		Iterator<WigItem> result = test.query("chrI", 5, 8);
		float[] data = WigFile.flattenData(result, 5, 8);
		assertEquals(4, data.length);
		float[] expected = {5.0f, 6.0f, 7.0f, 8.0f};
		assertArrayEquals(expected, data, 1e-7f);
	}
	
	@Test
	public void testMeanQuery() throws WigFileException, IOException {
		Iterator<WigItem> result = test.query("chrI", 5, 8);
		assertEquals(6.5, WigFile.mean(result, 5, 8), 1e-7);
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.WigFile#stdev(java.util.Iterator)}.
	 * @throws IOException 
	 * @throws WigFileException 
	 */
	@Test
	public void testStdevQuery() throws WigFileException, IOException {
		Iterator<WigItem> result = test.query("chrI", 5, 8);
		assertEquals(1.1180340051651, WigFile.stdev(result, 5, 8), 1e-7);
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.WigFile#min(java.util.Iterator)}.
	 * @throws IOException 
	 * @throws WigFileException 
	 */
	@Test
	public void testMinQuery() throws WigFileException, IOException {
		Iterator<WigItem> result = test.query("chrI", 5, 8);
		assertEquals(5, WigFile.min(result, 5, 8), 1e-7);
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.WigFile#max(java.util.Iterator)}.
	 * @throws IOException 
	 * @throws WigFileException 
	 */
	@Test
	public void testMaxQuery() throws WigFileException, IOException {
		Iterator<WigItem> result = test.query("chrI", 5, 8);
		assertEquals(8, WigFile.max(result, 5, 8), 1e-7);
	}

	@Test
	public void testQueryCount() throws WigFileException, IOException {
		Iterator<WigItem> result = test.query("chrI", 5, 8);
		int count = 0;
		while (result.hasNext()) {
			result.next();
			count++;
		}
		assertEquals(4, count);
	}
	
	@Test(expected = WigFileException.class)
	public void testQueryException() throws WigFileException, IOException {
		Iterator<WigItem> result = test.query("chrI", -2, 8);
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

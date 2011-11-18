/**
 * 
 */
package edu.unc.genomics.io;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author timpalpant
 *
 */
public class BigWigFileTest {
	
	private static final Path TEST_BIGWIG = Paths.get("fixtures/test.bw");
	private WigFile wig;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		wig = new BigWigFile(TEST_BIGWIG);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.BigWigFile#query(java.lang.String, int, int)}.
	 */
	@Test
	public void testQueryStringIntInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.BigWigFile#chromosomes()}.
	 */
	@Test
	public void testChromosomes() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.BigWigFile#getChrStart(java.lang.String)}.
	 */
	@Test
	public void testGetChrStart() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.BigWigFile#getChrStop(java.lang.String)}.
	 */
	@Test
	public void testGetChrStop() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.BigWigFile#includes(java.lang.String, int, int)}.
	 */
	@Test
	public void testIncludesStringIntInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.BigWigFile#includes(java.lang.String)}.
	 */
	@Test
	public void testIncludesString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.BigWigFile#numBases()}.
	 */
	@Test
	public void testNumBases() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.BigWigFile#total()}.
	 */
	@Test
	public void testTotal() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.BigWigFile#mean()}.
	 */
	@Test
	public void testMean() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.BigWigFile#stdev()}.
	 */
	@Test
	public void testStdev() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.BigWigFile#min()}.
	 */
	@Test
	public void testMin() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.BigWigFile#max()}.
	 */
	@Test
	public void testMax() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.BigWigFile#toString()}.
	 */
	@Test
	public void testToString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.BigWigFile#isBigWig(java.nio.file.Path)}.
	 */
	@Test
	public void testIsBigWig() {
		fail("Not yet implemented");
	}

}

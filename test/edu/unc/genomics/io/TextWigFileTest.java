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
public class TextWigFileTest {
	
	private static final Path TEST_WIG = Paths.get("fixtures/test.wig");
	private WigFile wig;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		wig = new TextWigFile(TEST_WIG);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	/**
	 * Test method for {@link edu.unc.genomics.io.TextWigFile#toString()}.
	 */
	@Test
	public void testToString() {
		wig.toString();
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.TextWigFile#query(java.lang.String, int, int)}.
	 */
	@Test
	public void testQueryStringIntInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.TextWigFile#chromosomes()}.
	 */
	@Test
	public void testChromosomes() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.TextWigFile#getChrStart(java.lang.String)}.
	 */
	@Test
	public void testGetChrStart() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.TextWigFile#getChrStop(java.lang.String)}.
	 */
	@Test
	public void testGetChrStop() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.TextWigFile#includes(java.lang.String, int, int)}.
	 */
	@Test
	public void testIncludesStringIntInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.TextWigFile#includes(java.lang.String)}.
	 */
	@Test
	public void testIncludesString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.TextWigFile#numBases()}.
	 */
	@Test
	public void testNumBases() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.TextWigFile#total()}.
	 */
	@Test
	public void testTotal() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.TextWigFile#mean()}.
	 */
	@Test
	public void testMean() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.TextWigFile#stdev()}.
	 */
	@Test
	public void testStdev() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.TextWigFile#min()}.
	 */
	@Test
	public void testMin() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.TextWigFile#max()}.
	 */
	@Test
	public void testMax() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.TextWigFile#writeExternal(java.io.ObjectOutput)}.
	 */
	@Test
	public void testWriteExternal() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.TextWigFile#readExternal(java.io.ObjectInput)}.
	 */
	@Test
	public void testReadExternal() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.WigFile#query(edu.unc.genomics.Interval)}.
	 */
	@Test
	public void testQueryInterval() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.unc.genomics.io.WigFile#includes(edu.unc.genomics.Interval)}.
	 */
	@Test
	public void testIncludesInterval() {
		fail("Not yet implemented");
	}

}

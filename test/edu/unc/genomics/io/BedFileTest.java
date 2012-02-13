package edu.unc.genomics.io;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.unc.genomics.BedEntry;
import edu.unc.genomics.Interval;

public class BedFileTest extends AbstractIntervalFileTest {

	public static final Path TEST_BED = Paths.get("test/fixtures/test.bed");

	@Before
	public void setUp() throws Exception {
		test = new BedFile(TEST_BED);
	}

	@Test
	public void testCount() {
		assertEquals(10, test.count());
	}

	@Test
	public void testChromosomes() {
		assertTrue(test.chromosomes().contains("chrI"));
		assertTrue(test.chromosomes().contains("chrII"));
		assertTrue(test.chromosomes().contains("chrIII"));
		assertTrue(test.chromosomes().contains("chrIV"));
		assertEquals(4, test.chromosomes().size());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testQuery() {
		test.query("chrI", 10, 1000);
	}

	@Test
	public void testIterator() {
		int count = 0;
		for (Interval entry : test) {
			count++;
		}
		assertEquals(10, count);
	}

	@Test
	public void testLoadAll() {
		List<? extends Interval> all = test.loadAll();
		assertEquals(10, all.size());
	}

}

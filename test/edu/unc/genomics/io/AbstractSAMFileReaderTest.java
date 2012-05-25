package edu.unc.genomics.io;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import edu.unc.genomics.Interval;

public class AbstractSAMFileReaderTest extends AbstractIntervalFileReaderTest {

	@Test
	public void testCount() {
		assertEquals(69, test.count());
	}

	@Test
	public void testChromosomes() {
		assertTrue(test.chromosomes().contains("chrI"));
		assertTrue(test.chromosomes().contains("chrII"));
		assertTrue(test.chromosomes().contains("chrIII"));
		assertTrue(test.chromosomes().contains("chrIV"));
		assertEquals(18, test.chromosomes().size());
	}

	@Test
	public void testQuery() {
		Iterator<? extends Interval> it = test.query("chrXII", 460000, 470000);
		int count = 0;
		while(it.hasNext()) {
			it.next();
			count++;
		}
		assertEquals(7, count);
	}

	@Test
	public void testIterator() {
		int count = 0;
		for (Interval entry : test) {
			count++;
		}
		assertEquals(69, count);
	}

	@Test
	public void testLoadAll() {
		List<? extends Interval> all = test.loadAll();
		assertEquals(69, all.size());
	}

}

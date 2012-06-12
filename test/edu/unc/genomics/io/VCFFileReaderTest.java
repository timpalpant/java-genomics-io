package edu.unc.genomics.io;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import edu.unc.genomics.Interval;

public class VCFFileReaderTest extends AbstractIntervalFileReaderTest {

	public static final Path TEST_VCF = Paths.get("test/fixtures/test.vcf");

	@Before
	public void setUp() throws Exception {
		test = new VCFFileReader(TEST_VCF);
	}

	@Test
	public void testCount() {
		assertEquals(1007, test.count());
	}

	@Test
	public void testChromosomes() {
		assertEquals(17, test.chromosomes().size());
	}

	@Test
	public void testQuery() {
		Iterator<? extends Interval> it = test.query("chrXVI", 900_000, 920_000);
		int count = 0;
		while (it.hasNext()) {
			it.next();
			count++;
		}
		assertEquals(2, count);
	}

	@Test
	public void testIterator() {
		int count = 0;
		for (Interval interval : test) {
			count++;
		}
		assertEquals(1007, count);
	}
}

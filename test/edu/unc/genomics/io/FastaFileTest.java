package edu.unc.genomics.io;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import edu.unc.genomics.Sequence;

public class FastaFileTest extends AbstractSequenceFileTest {

	public static final Path TEST_FASTA = Paths.get("test/fixtures/test.fa");

	@Before
	public void setUp() throws Exception {
		test = new FastaFile(TEST_FASTA);
	}

	@Test
	public void testIterator() {
		int count = 0;
		for (Sequence s : test) {
			count++;
		}
		assertEquals(2, count);
	}

}

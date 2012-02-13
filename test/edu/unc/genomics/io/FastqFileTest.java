package edu.unc.genomics.io;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import edu.unc.genomics.Sequence;

public class FastqFileTest extends AbstractSequenceFileTest {
	
	public static final Path TEST_FASTQ = Paths.get("test/fixtures/test.fastq");

	@Before
	public void setUp() throws Exception {
		test = new FastqFile(TEST_FASTQ);
	}

	@Test
	public void testIterator() {
		int count = 0;
		for (Sequence s : test) {
			count++;
		}
		assertEquals(94, count);
	}

}

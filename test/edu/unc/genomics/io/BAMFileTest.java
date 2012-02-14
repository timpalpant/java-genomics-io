package edu.unc.genomics.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import edu.unc.genomics.util.Samtools;

public class BAMFileTest extends SAMFileTest {

	public static final Path TEST_BAM = Paths.get("test/fixtures/test.bam");

	@Before
	public void setUp() throws Exception {
		test = new SAMFile(TEST_BAM);
	}

	@AfterClass
	public static void cleanUp() throws Exception {
		Files.deleteIfExists(Samtools.findIndexFile(TEST_BAM));
	}
	
	@Override
	@Test
	public void testQuery() {
		test.query("chrI", 10, 1000);
	}
}

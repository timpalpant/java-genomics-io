package edu.unc.genomics.io;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;

public class BAMFileReaderTest extends AbstractSAMFileReaderTest {

	public static final Path TEST_BAM = Paths.get("test/fixtures/test.bam");

	@Before
	public void setUp() throws Exception {
		test = new BAMFileReader(TEST_BAM);
	}

}

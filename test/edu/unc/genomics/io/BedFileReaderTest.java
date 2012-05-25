package edu.unc.genomics.io;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;

public class BedFileReaderTest extends AbstractBedFileReaderTest {

	public static final Path TEST_BED = Paths.get("test/fixtures/test.bed");

	@Before
	public void setUp() throws Exception {
		test = new BedFileReader(TEST_BED);
	}

}

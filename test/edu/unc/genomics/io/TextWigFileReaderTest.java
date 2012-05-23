package edu.unc.genomics.io;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;

public class TextWigFileReaderTest extends AbstractWigFileReaderTest {

	public static final Path TEST_WIG = Paths.get("test/fixtures/test.wig");
	
	@Before
	public void setUp() throws Exception {
		test = new TextWigFileReader(TEST_WIG);
	}
	
}

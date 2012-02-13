package edu.unc.genomics.io;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;

public class BigWigFileTest extends AbstractWigFileTest {

	public static final Path TEST_BIGWIG = Paths.get("test/fixtures/test.bw");
	
	@Before
	public void setUp() throws Exception {
		test = new BigWigFile(TEST_BIGWIG);
	}
	
}

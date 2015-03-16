package edu.unc.genomics.io;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;

public class SAMFileReaderTest extends AbstractSAMFileReaderTest {

  public static final Path TEST_SAM = Paths.get("test/fixtures/test.sam");

  @Before
  public void setUp() throws Exception {
    test = new SAMFileReader(TEST_SAM);
  }

}

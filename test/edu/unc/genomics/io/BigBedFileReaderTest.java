package edu.unc.genomics.io;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;

public class BigBedFileReaderTest extends AbstractBedFileReaderTest {

  public static final Path TEST_BIGBED = Paths.get("test/fixtures/test.bb");

  @Before
  public void setUp() throws Exception {
    test = new BigBedFileReader(TEST_BIGBED);
  }

}

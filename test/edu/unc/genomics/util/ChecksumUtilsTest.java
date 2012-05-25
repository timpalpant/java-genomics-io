package edu.unc.genomics.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import edu.unc.genomics.util.ChecksumUtils;

public class ChecksumUtilsTest {
	
	private static final Path TEST = Paths.get("test/fixtures/test.wig");

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testAdler32() throws IOException {
		assertEquals(3238352161L, ChecksumUtils.adler32(TEST));
	}

	@Test
	public void testCrc32() throws IOException {
		assertEquals(1665388664L, ChecksumUtils.crc32(TEST));
	}

}

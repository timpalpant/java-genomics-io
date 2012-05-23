package edu.genomics.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import edu.unc.genomics.util.FileUtils;

public class FileUtilsTest {
	
	private Path testAscii = Paths.get("test/fixtures/test.fa");
	private Path testBinary = Paths.get("test/fixtures/test.bam");

	@Test
	public void testIsAsciiText() throws IOException {
		assertTrue(FileUtils.isAsciiText(testAscii));
		assertFalse(FileUtils.isAsciiText(testBinary));
	}

	@Test
	public void testIsAsciiTextWithThreshold() throws IOException {
		assertTrue(FileUtils.isAsciiText(testAscii, 1.0));
	}

	@Test
	public void testCountLines() throws IOException {
		assertEquals(122, FileUtils.countLines(testAscii));
	}

}

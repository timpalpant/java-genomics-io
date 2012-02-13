package edu.genomics.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import edu.unc.genomics.util.FileUtils;

public class FileUtilsTest {
	
	private Path test = Paths.get("test/fixtures/test.fa");

	@Test
	public void testIsAsciiText() throws IOException {
		assertTrue(FileUtils.isAsciiText(test));
	}

	@Test
	public void testIsAsciiTextWithThreshold() throws IOException {
		assertTrue(FileUtils.isAsciiText(test, 1.0));
	}

	@Test
	public void testCountLines() throws IOException {
		assertEquals(122, FileUtils.countLines(test));
	}

}

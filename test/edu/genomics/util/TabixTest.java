package edu.genomics.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.sf.samtools.TabixReader;
import net.sf.samtools.TabixWriter;
import net.sf.samtools.TabixWriter.TabixException;

import org.junit.After;
import org.junit.Test;

import edu.unc.genomics.util.Tabix;

public class TabixTest {
	
	private static final Path testSorted = Paths.get("test/fixtures/test.bed.sorted");
	private static final Path testBgzipped = Paths.get("test/fixtures/test.bed.sorted.bz");
	private static final Path testIndex = testBgzipped.resolveSibling(testBgzipped.getFileName()+TabixReader.DEFAULT_INDEX_EXTENSION);

	@After
	public void tearDown() throws Exception {
		Files.deleteIfExists(testBgzipped);
		Files.deleteIfExists(testIndex);
	}

	@Test
	public void testBgzip() throws IOException {
		Tabix.bgzip(testSorted, testBgzipped);
		assertTrue(Files.exists(testBgzipped));
	}
	
	@Test
	public void testIndex() throws IOException, TabixException {
		Tabix.bgzip(testSorted, testBgzipped);
		Tabix.index(testBgzipped, TabixWriter.BED_CONF);
		assertTrue(Files.exists(testIndex));
	}

}

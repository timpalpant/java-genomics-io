package edu.unc.genomics.util;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Test;

import edu.unc.genomics.util.Samtools;

public class SamtoolsTest {
	
	private static final Path testSAM = Paths.get("test/fixtures/test.sam");
	private static final Path testBAM = Paths.get("test/fixtures/test.bam");
	private static final Path testOutput = Paths.get("test/fixtures/test.output.tmp");
	private static final Path testBAMIndex = Paths.get("test/fixtures/test.bam.bai");
	
	@After
	public void tearDown() throws Exception {
		Files.deleteIfExists(testOutput);
		Files.deleteIfExists(testBAMIndex);
	}

	@Test
	public void testSamToBam() {
		Samtools.samToBam(testSAM, testOutput);
		assertTrue(Files.exists(testOutput));
	}

	@Test
	public void testIndexBAMFile() {
		Samtools.indexBAMFile(testBAM, testBAMIndex);
		assertTrue(Files.exists(testBAMIndex));
	}

	@Test
	public void testFindIndexFile() {
		assertNull(Samtools.findIndexFile(testBAM));
		
		Samtools.indexBAMFile(testBAM, testBAMIndex);
		assertEquals(testBAMIndex, Samtools.findIndexFile(testBAM));
	}

}

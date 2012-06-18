package edu.unc.genomics.util;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import net.sf.samtools.TabixWriter;
import net.sf.samtools.TabixWriter.Conf;

import org.junit.After;
import org.junit.Test;

import edu.unc.genomics.util.FileUtils;

public class FileUtilsTest {
	
	private static final Path testAscii = Paths.get("test/fixtures/test.fa");
	private static final Path testBinary = Paths.get("test/fixtures/test.bam");
	private static final Path testBed = Paths.get("test/fixtures/test.bed");
	private static final Path testSorted = Paths.get("test/fixtures/test.bed.sorted");
	private static final Path testOutput = Paths.get("test/fixtures/test.bed.tmp");

	@After
	public void tearDown() throws IOException {
		Files.deleteIfExists(testOutput);
	}
	
	@Test
	public void testIsAsciiText() throws IOException {
		assertTrue(FileUtils.isAsciiText(testAscii));
		assertFalse(FileUtils.isAsciiText(testBinary));
	}

	@Test
	public void testCountLines() throws IOException {
		assertEquals(122, FileUtils.countLines(testAscii));
	}

	@Test
	public void testSort() throws IOException {
		// Bed Entry comparator
		Comparator<String> bedComp = new Comparator<String>() {
			public int compare(final String s1, final String s2) {
				Conf conf = TabixWriter.BED_CONF;
				
				// Parse the two lines into intervals
				String[] entry1 = s1.split("\t");
				String[] entry2 = s2.split("\t");
				
				// First sort by chromosome
				String chr1 = entry1[conf.chrColumn-1];
				String chr2 = entry2[conf.chrColumn-1];
				int c1 = chr1.compareTo(chr2);
				if (c1 != 0) {
					return c1;
				}
				
				// Then sort by start
				Integer start1 = Integer.valueOf(entry1[conf.startColumn-1]);
				Integer start2 = Integer.valueOf(entry2[conf.startColumn-1]);
				int c2 = start1.compareTo(start2);
				if (c2 != 0) {
					return c2;
				}
				
				// Then sort by end
				Integer stop1 = Integer.valueOf(entry1[conf.endColumn-1]);
				Integer stop2 = Integer.valueOf(entry2[conf.endColumn-1]);
				int c3 = stop1.compareTo(stop2);
				
				// If they are still equal at this point, then they are equal
				return c3;
			}
		};
		
		FileUtils.sort(testBed, testOutput, bedComp);
		
		// Diff the expected and actual results
		try (BufferedReader output = Files.newBufferedReader(testOutput, Charset.defaultCharset());
				 BufferedReader expected = Files.newBufferedReader(testSorted, Charset.defaultCharset())) {
			String outputLine, expectedLine;
			int lineNum = 1;
			while ((outputLine = output.readLine()) != null) {
				expectedLine = expected.readLine();
				assertEquals("Sorted output differs from expected at line "+(lineNum++), expectedLine, outputLine);
			}
		}
	}
}

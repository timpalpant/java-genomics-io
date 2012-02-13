package edu.genomics;

import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import edu.unc.genomics.Assembly;

public class AssemblyTest {
	
	private Assembly test;
	private String[] chromosomes = {"chrIV", "chrXV", "chrVII", "chrXII", "chrXVI", "chrXIII", "chrII", "chrXIV", "chrX", "chrXI", "chrV", "chrVIII", "chrIX", "chrIII", "chrVI", "chrI", "chrM", "2micron"};
	private int[] lengths = {1531919, 1091289, 1090947, 1078175, 948062, 924429, 813178, 784333, 745742, 666454, 576869, 562643, 439885, 316617, 270148, 230208, 85779, 6318};
	
	@Before
	public void setUp() throws Exception {
		test = new Assembly(Paths.get("test/fixtures/test.len"));
	}

	@Test
	public void testChromosomes() {
		assertEquals(18, test.chromosomes().size());
		for (String chr : chromosomes) {
			assertTrue(test.chromosomes().contains(chr));
		}
	}

	@Test
	public void testIncludes() {
		for (String chr : chromosomes) {
			assertTrue(test.includes(chr));
		}
	}

	@Test
	public void testGetChrLength() {
		for (int i = 0; i < chromosomes.length; i++) {
			assertEquals(lengths[i], test.getChrLength(chromosomes[i]).intValue());
		}
	}

	@Test
	public void testIterator() {
		Set<String> chrSet = new HashSet<String>(Arrays.asList(chromosomes));
		int count = 0;
		for (String chr : test) {
			count++;
			assertTrue(chrSet.contains(chr));
		}
		assertEquals(chrSet.size(), count);
	}

}

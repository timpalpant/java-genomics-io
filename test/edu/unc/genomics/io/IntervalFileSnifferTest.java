package edu.unc.genomics.io;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class IntervalFileSnifferTest {
	
	private static final Path TEST_ASCII = Paths.get("test/fixtures/test.bed");
	private static final Path TEST_BINARY = Paths.get("test/fixtures/test.bam");
	private static final Path TEST_BIGBED = Paths.get("test/fixtures/test.bb");
	private static final Path TEST_BED = Paths.get("test/fixtures/test.bed");
	private static final Path TEST_BEDGRAPH = Paths.get("test/fixtures/test.bedGraph");
	private static final Path TEST_GFF = Paths.get("test/fixtures/test.gff");
	private static final Path TEST_GENETRACK = Paths.get("test/fixtures/test.genetrack");
	private static final Path TEST_BAM = Paths.get("test/fixtures/test.bam");
	private static final Path TEST_SAM = Paths.get("test/fixtures/test.sam");

	IntervalFileSniffer sniffer;

	@Test
	public void testIsAscii() throws IOException {
		sniffer = new IntervalFileSniffer(TEST_ASCII);
		assertTrue(sniffer.isAscii());
		assertFalse(sniffer.isBinary());
	}

	@Test
	public void testIsBinary() throws IOException {
		sniffer = new IntervalFileSniffer(TEST_BINARY);
		assertTrue(sniffer.isBinary());
		assertFalse(sniffer.isAscii());
	}

	@Test
	public void testIsBigBed() throws IOException {
		sniffer = new IntervalFileSniffer(TEST_BIGBED);
		assertTrue(sniffer.isBigBed());
		assertFalse(sniffer.isBAM());
		assertFalse(sniffer.isBed());
		assertFalse(sniffer.isBedGraph());
		assertFalse(sniffer.isGeneTrack());
		assertFalse(sniffer.isGFF());
		assertFalse(sniffer.isSAM());
	}

	@Test
	public void testIsBed() throws IOException {
		sniffer = new IntervalFileSniffer(TEST_BED);
		assertTrue(sniffer.isBed());
		assertFalse(sniffer.isBAM());
		assertFalse(sniffer.isBigBed());
		assertFalse(sniffer.isBedGraph());
		assertFalse(sniffer.isGeneTrack());
		assertFalse(sniffer.isGFF());
		assertFalse(sniffer.isSAM());
	}

	@Test
	public void testIsBedGraph() throws IOException {
		sniffer = new IntervalFileSniffer(TEST_BEDGRAPH);
		assertTrue(sniffer.isBedGraph());
		assertFalse(sniffer.isBAM());
		assertFalse(sniffer.isBed());
		assertFalse(sniffer.isBigBed());
		assertFalse(sniffer.isGeneTrack());
		assertFalse(sniffer.isGFF());
		assertFalse(sniffer.isSAM());
	}

	@Test
	public void testIsGFF() throws IOException {
		sniffer = new IntervalFileSniffer(TEST_GFF);
		assertTrue(sniffer.isGFF());
		assertFalse(sniffer.isBAM());
		assertFalse(sniffer.isBed());
		assertFalse(sniffer.isBedGraph());
		assertFalse(sniffer.isGeneTrack());
		assertFalse(sniffer.isBigBed());
		assertFalse(sniffer.isSAM());
	}

	@Test
	public void testIsGeneTrack() throws IOException {
		sniffer = new IntervalFileSniffer(TEST_GENETRACK);
		assertTrue(sniffer.isGeneTrack());
		assertFalse(sniffer.isBAM());
		assertFalse(sniffer.isBed());
		assertFalse(sniffer.isBedGraph());
		assertFalse(sniffer.isBigBed());
		assertFalse(sniffer.isGFF());
		assertFalse(sniffer.isSAM());
	}

	@Test
	public void testIsBAM() throws IOException {
		sniffer = new IntervalFileSniffer(TEST_BAM);
		assertTrue(sniffer.isBAM());
		assertFalse(sniffer.isBigBed());
		assertFalse(sniffer.isBed());
		assertFalse(sniffer.isBedGraph());
		assertFalse(sniffer.isGeneTrack());
		assertFalse(sniffer.isGFF());
		assertFalse(sniffer.isSAM());
	}

	@Test
	public void testIsSAM() throws IOException {
		sniffer = new IntervalFileSniffer(TEST_SAM);
		assertTrue(sniffer.isSAM());
		assertFalse(sniffer.isBAM());
		assertFalse(sniffer.isBed());
		assertFalse(sniffer.isBedGraph());
		assertFalse(sniffer.isGeneTrack());
		assertFalse(sniffer.isGFF());
		assertFalse(sniffer.isBigBed());
	}

}

package edu.unc.genomics.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import edu.unc.util.FileUtils;
import edu.unc.util.NumberUtils;

import net.sf.samtools.SAMFileReader;

public class IntervalFileSniffer {
	protected Path p;
	protected String firstLine;
	
	public IntervalFileSniffer(Path p) {
		this.p = p;
	}
	
	public static Class<? extends IntervalFile> sniff(Path p) {
		return (new IntervalFileSniffer(p)).autodetect();
	}
	
	public Class<? extends IntervalFile> autodetect() {
		Class<? extends IntervalFile> clazz;
		
		if (isBed()) {
			clazz = BedFile.class;
		} else if (isBedGraph()) {
			clazz = BedGraphFile.class;
		} else if (isSAM()) {
			clazz = SAMFile.class;
		} else if (isBAM()) {
			clazz = BAMFile.class;
		} else {
			throw new IntervalFileSnifferException("Could not auto-detect interval file type");
		}
		
		return clazz;
	}
	
	protected boolean isAscii() throws IOException {
		return FileUtils.isAsciiText(p);
	}
	
	protected boolean isBinary() throws IOException {
		return !isAscii();
	}
	
	protected boolean isBed() throws IOException {
		if (isBinary()) { return false; }
		if (numColumns() < 3 || numColumns() > 12) { return false; }
		if (!NumUtils.isInteger(column(2)) || !NumUtils.isInteger(column(3))) { return false; }
		if (numColumns() == 4 && NumUtils.isNumeric(column(4))) { return false; }
		
		try { 
			// Parse BedEntry
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	protected boolean isBedGraph() throws IOException {
		if (isBinary()) { return false; }
		if (numColumns() != 4) { return false; }
		if (!NumUtils.isInteger(column(2)) || !NumUtils.isInteger(column(3))) { return false; }
		if (!NumUtils.isNumeric(column(4))) { return false; }
		
		try { 
			// Parse BedGraphEntry
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	protected boolean isSAM() throws IOException {
		SAMFileReader reader = new SAMFileReader(p.toFile());
		return !reader.isBinary();
	}
	
	protected boolean isBAM() {
		SAMFileReader reader = new SAMFileReader(p.toFile());
		return reader.isBinary();
	}
	
	protected String getFirstLine() throws IntervalFileSnifferException {
		if (firstLine == null) {
			try {
				BufferedReader reader = Files.newBufferedReader(p, Charset.defaultCharset());
				firstLine = reader.readLine();
				
				while (firstLine.length() == 0 || firstLine.startsWith("track")) {
					firstLine = reader.readLine();
				}
				
				reader.close();
			} catch (Exception e) {
				throw new IntervalFileSnifferException("Error attempting to read first line of file");
			}
		}
		
		return firstLine;
	}
	
	protected int numColumns() {
		return firstLine.split("\t").length;
	}
	
	protected String column(int n) {
		return firstLine.split("\t")[n-1];
	}
}
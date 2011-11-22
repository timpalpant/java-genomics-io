package edu.unc.genomics.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.broad.igv.bbfile.BBFileReader;

import edu.unc.genomics.BedEntry;
import edu.unc.genomics.BedGraphEntry;
import edu.unc.genomics.util.FileUtils;
import edu.unc.genomics.util.NumUtils;

import net.sf.samtools.SAMFileReader;

public class IntervalFileSniffer {
	protected Path p;
	protected String firstLine;
	
	public IntervalFileSniffer(Path p) {
		this.p = p;
	}
	
	public boolean isAscii() throws IntervalFileSnifferException {
		try {
			return FileUtils.isAsciiText(p);
		} catch (IOException e) {
			throw new IntervalFileSnifferException("IOException while attempting to determine if file is binary");
		}
	}
	
	public boolean isBinary() throws IntervalFileSnifferException {
		return !isAscii();
	}
	
	public boolean isBigBed() {
		try {
			BBFileReader reader = new BBFileReader(p.toString());
			return reader.isBigBedFile();
		} catch (IOException e) {
			return false;
		}
	}
	
	public boolean isBed() throws IntervalFileSnifferException {
		if (!isAscii()) { return false; }
		if (numColumns() < 3 || numColumns() > 12) { return false; }
		if (!NumUtils.isInteger(column(2)) || !NumUtils.isInteger(column(3))) { return false; }
		if (numColumns() == 4 && NumUtils.isNumeric(column(4))) { return false; }
		
		try { 
			BedEntry.parse(getFirstLine());
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	public boolean isBedGraph() throws IntervalFileSnifferException {
		if (!isAscii()) { return false; }
		if (numColumns() != 4) { return false; }
		if (!NumUtils.isInteger(column(2)) || !NumUtils.isInteger(column(3))) { return false; }
		if (!NumUtils.isNumeric(column(4))) { return false; }
		
		try { 
			BedGraphEntry.parse(getFirstLine());
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	public boolean isBAM() {
		try {
			SAMFileReader.setDefaultValidationStringency(SAMFileReader.ValidationStringency.STRICT);
			SAMFileReader reader = new SAMFileReader(p.toFile());
			return reader.isBinary();
		} catch (Exception e) {
			return false;
		} finally {
			SAMFileReader.setDefaultValidationStringency(SAMFileReader.ValidationStringency.DEFAULT_STRINGENCY);
		}
	}
	
	public boolean isSAM() throws IntervalFileSnifferException {
		if (!isAscii()) { return false; }
		if (numColumns() < 4) { return false; }
		if (!NumUtils.isInteger(column(4))) { return false; }
		
		// TODO: Better checking for SAM files
		/*try {
		SAMFileReader.setDefaultValidationStringency(SAMFileReader.ValidationStringency.STRICT);
		SAMFileReader reader = new SAMFileReader(p.toFile());
			return !reader.isBinary();
		} catch (Exception e) {
			return false;
		} finally {
			SAMFileReader.setDefaultValidationStringency(SAMFileReader.ValidationStringency.DEFAULT_STRINGENCY);
		}*/
		
		return true;
	}
	
	private String getFirstLine() throws IntervalFileSnifferException {
		if (firstLine == null) {
			try {
				BufferedReader reader = Files.newBufferedReader(p, Charset.defaultCharset());
				firstLine = reader.readLine();
				
				while (firstLine.length() == 0 || firstLine.startsWith("track") 
						|| firstLine.startsWith("#") || firstLine.startsWith("@")) {
					firstLine = reader.readLine();
				}
				
				reader.close();
			} catch (Exception e) {
				throw new IntervalFileSnifferException("Error attempting to read first line of file");
			}
		}
		
		return firstLine;
	}
	
	private int numColumns() throws IntervalFileSnifferException {
		return getFirstLine().split("\t").length;
	}
	
	private String column(int n) throws IntervalFileSnifferException {
		return getFirstLine().split("\t")[n-1];
	}
}
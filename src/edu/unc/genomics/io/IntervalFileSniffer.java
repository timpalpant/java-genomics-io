package edu.unc.genomics.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.broad.igv.bbfile.BBFileReader;

import edu.unc.genomics.BedEntry;
import edu.unc.genomics.BedGraphEntry;
import edu.unc.genomics.GFFEntry;
import edu.unc.genomics.util.FileUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileReader.ValidationStringency;
import net.sf.samtools.SAMRecord;

public class IntervalFileSniffer {
	
	private static final Logger log = Logger.getLogger(IntervalFileSniffer.class);
	
	protected Path p;
	protected String firstLine;
	
	public IntervalFileSniffer(Path p) {
		this.p = p;
	}
	
	public boolean isAscii() throws IOException {
		return FileUtils.isAsciiText(p);
	}
	
	public boolean isBinary() throws IOException {
		return !isAscii();
	}
	
	public boolean isBigBed() {
		try {
			BBFileReader reader = new BBFileReader(p.toString());
			return reader.isBigBedFile();
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean isBed() throws IOException {
		if (!isAscii()) { return false; }
		if (numColumns() < 3 || numColumns() > 12) { return false; }
		if (!StringUtils.isNumeric(column(2)) || !StringUtils.isNumeric(column(3))) { return false; }
		if (numColumns() == 4 && StringUtils.isNumeric(column(4))) { return false; }
		
		try { 
			BedEntry.parse(getFirstLine());
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	public boolean isBedGraph() throws IOException {
		if (!isAscii()) { return false; }
		if (numColumns() != 4) { return false; }
		if (!StringUtils.isNumeric(column(2)) || !StringUtils.isNumeric(column(3))) { return false; }
		if (!StringUtils.isNumeric(column(4))) { return false; }
		
		try { 
			BedGraphEntry.parse(getFirstLine());
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	public boolean isGFF() throws IOException {
		if (!isAscii()) { return false; }
		if (numColumns() < 9) { return false; }
		if (!StringUtils.isNumeric(column(4)) || !StringUtils.isNumeric(column(5))) { return false; }
		
		try { 
			GFFEntry.parse(getFirstLine());
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	public boolean isBAM() {
		boolean isBam = false;
		
		ValidationStringency stringency = SAMFileReader.getDefaultValidationStringency();
		try {
			SAMFileReader.setDefaultValidationStringency(SAMFileReader.ValidationStringency.STRICT);
			SAMFileReader reader = new SAMFileReader(p.toFile());
			// Ensure that the first record loads correctly
			SAMRecord r = reader.iterator().next();
			isBam = reader.isBinary();
			reader.close();
		} catch (Exception e) {
			return false;
		} finally {
			SAMFileReader.setDefaultValidationStringency(stringency);
		}
		
		return isBam;
	}
	
	public boolean isSAM() throws IOException {
		boolean isSAM = false;
		
		ValidationStringency stringency = SAMFileReader.getDefaultValidationStringency();
		try {
			SAMFileReader.setDefaultValidationStringency(SAMFileReader.ValidationStringency.STRICT);
			SAMFileReader reader = new SAMFileReader(p.toFile());
			// Ensure that the first record loads correctly
			SAMRecord r = reader.iterator().next();
			isSAM = !reader.isBinary();
			reader.close();
		} catch (Exception e) {
			return false;
		} finally {
			SAMFileReader.setDefaultValidationStringency(stringency);
		}
		
		return isSAM;
	}
	
	private String getFirstLine() throws IOException {
		if (firstLine == null) {
			try (BufferedReader reader = Files.newBufferedReader(p, Charset.defaultCharset())) {
				firstLine = reader.readLine();
				
				while (firstLine != null && (firstLine.length() == 0 || firstLine.startsWith("track") 
						|| firstLine.startsWith("#") || firstLine.startsWith("@"))) {
					firstLine = reader.readLine();
				}
			}
		}
		
		return firstLine;
	}
	
	private int numColumns() throws IOException {
		return getFirstLine().split("\t").length;
	}
	
	private String column(int n) throws IOException {
		return getFirstLine().split("\t")[n-1];
	}
}
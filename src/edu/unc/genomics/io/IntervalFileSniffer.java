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

/**
 * Methods for "sniffing" file types to try to guess what format a file is
 * 
 * @author timpalpant
 *
 */
public class IntervalFileSniffer {
	
	private static final Logger log = Logger.getLogger(IntervalFileSniffer.class);
	
	protected Path p;
	protected String firstLine;
	
	public IntervalFileSniffer(Path p) {
		this.p = p;
	}
	
	/**
	 * @return true if this file is ASCII-text, false otherwise
	 * @throws IOException
	 */
	public boolean isAscii() throws IOException {
		return FileUtils.isAsciiText(p);
	}
	
	/**
	 * @return true if this file is not ASCII-text
	 * @throws IOException
	 */
	public boolean isBinary() throws IOException {
		return !isAscii();
	}
	
	/**
	 * @return true if this file is a BigBed file
	 */
	public boolean isBigBed() {
		try {
			BBFileReader reader = new BBFileReader(p.toString());
			return reader.isBigBedFile();
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * @return true if this file is Bed format, but not BedGraph format
	 * @throws IOException
	 */
	public boolean isBed() throws IOException {
		if (!isAscii()) { return false; }
		if (numColumns() < 3 || numColumns() > 12) { return false; }
		if (!StringUtils.isNumeric(column(2)) || !StringUtils.isNumeric(column(3))) { return false; }
		if (isBedGraph()) { return false; }
		
		try { 
			BedEntry.parse(getFirstLine());
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * @return true if this file is BedGraph format
	 * @throws IOException
	 */
	public boolean isBedGraph() throws IOException {
		if (!isAscii()) { return false; }
		if (numColumns() != 4) { return false; }
		if (!StringUtils.isNumeric(column(2)) || !StringUtils.isNumeric(column(3))) { return false; }
		try {
			Float.parseFloat(column(4));
		} catch (Exception e) {
			return false;
		}
		
		try { 
			BedGraphEntry.parse(getFirstLine());
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * @return true if this file is GFF format
	 * @throws IOException
	 */
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
	
	/**
	 * @return true if this file is GeneTrack format (which is detected by the header line)
	 * @throws IOException
	 */
	public boolean isGeneTrack() throws IOException {
		if (!isAscii()) { return false; }
		if (numColumns() != 4) { return false; }
		if (column(1).equalsIgnoreCase("chrom") && column(2).equalsIgnoreCase("index")
				&& column(3).equalsIgnoreCase("forward") && column(4).equalsIgnoreCase("reverse")) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * @return true if this file is BAM format (with STRICT validation)
	 */
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
	
	/**
	 * @return true if this file is SAM format (with STRICT validation)
	 * @throws IOException
	 */
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
	
	/**
	 * @return the first non-comment line of an ASCII text file (comments = track, #, @)
	 * @throws IOException
	 */
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
	
	/**
	 * @return the number of tab-delimited columns in this ASCII-text file
	 * @throws IOException
	 */
	private int numColumns() throws IOException {
		return getFirstLine().split("\t").length;
	}
	
	/**
	 * @param n the column to return
	 * @return the value of the nth column of the first line in this file
	 * @throws IOException
	 */
	private String column(int n) throws IOException {
		return getFirstLine().split("\t")[n-1];
	}
}
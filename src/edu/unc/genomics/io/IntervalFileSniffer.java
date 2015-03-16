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
import edu.unc.genomics.VCFEntry;
import edu.unc.genomics.util.FileUtils;

import edu.ucsc.genome.TrackHeader;
import edu.ucsc.genome.TrackHeaderException;

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
  protected TrackHeader trackHeader;
  protected Boolean isAscii;

  public IntervalFileSniffer(Path p) {
    this.p = p;
  }

  /**
   * Print debugging info about why this file cannot be autodeteted
   */
  public void diagnose() throws IOException {
    log.info("Diagnosing " + p);
    if (isAscii()) {
      log.info("File appears to be ASCII");
      log.info("Data line used for diagnosis: " + getFirstLine());
      try {
        log.info("Assessing if file is GFF format");
        sniffGFF();
      } catch (IntervalFileSnifferException e) {
        log.info(e.getMessage());
      }
      try {
        log.info("Assessing if file is BedGraph format");
        sniffBedGraph();
      } catch (IntervalFileSnifferException e) {
        log.info(e.getMessage());
      }
      try {
        log.info("Assessing if file is Bed format");
        sniffBed();
      } catch (IntervalFileSnifferException e) {
        log.info(e.getMessage());
      }
      try {
        log.info("Assessing if file is SAM format");
        sniffSAM();
      } catch (IntervalFileSnifferException e) {
        log.info(e.getMessage());
      }
      try {
        log.info("Assessing if file is GeneTrack format");
        sniffGeneTrack();
      } catch (IntervalFileSnifferException e) {
        log.info(e.getMessage());
      }
      try {
        log.info("Assessing if file is VCF format");
        sniffVCF();
      } catch (IntervalFileSnifferException e) {
        log.info(e.getMessage());
      }
    } else {
      log.info("File appears to be binary");
      try {
        log.info("Assessing if file is BigBed format");
        sniffBigBed();
      } catch (IntervalFileSnifferException e) {
        log.info(e.getMessage());
      }
      try {
        log.info("Assessing if file is BAM format");
        sniffBAM();
      } catch (IntervalFileSnifferException e) {
        log.info(e.getMessage());
      }
    }
  }

  /**
   * @return true if this file is ASCII-text, false otherwise
   * @throws IOException
   */
  public Boolean isAscii() throws IOException {
    if (isAscii == null) {
      isAscii = FileUtils.isAsciiText(p);
    }

    return isAscii;
  }

  /**
   * @return true if this file is not ASCII-text
   * @throws IOException
   */
  public Boolean isBinary() throws IOException {
    return !isAscii();
  }

  /**
   * @return true if this file is a BigBed file
   */
  public boolean isBigBed() {
    try {
      return sniffBigBed();
    } catch (IntervalFileSnifferException e) {
      return false;
    }
  }

  /**
   * Analyze why this is or is not a BigBed file
   * 
   * @return true if this file is BigBed format
   * @throws IntervalFileSnifferException
   *           if this is not a BigBed file
   */
  public boolean sniffBigBed() throws IntervalFileSnifferException {
    try {
      BBFileReader reader = new BBFileReader(p.toString());
      return reader.isBigBedFile();
    } catch (Exception e) {
      throw new IntervalFileSnifferException("Error opening BigBed file: " + e.getMessage());
    }
  }

  /**
   * @return true if this file is Bed format, but not BedGraph format
   * @throws IOException
   */
  public boolean isBed() throws IOException {
    try {
      return sniffBed();
    } catch (IntervalFileSnifferException e) {
      return false;
    }
  }

  /**
   * Analyze why this is or is not a Bed file
   * 
   * @return true if this file is Bed format
   * @throws IntervalFileSnifferException
   *           if this is not a Bed file
   */
  public boolean sniffBed() throws IOException, IntervalFileSnifferException {
    if (!isAscii()) {
      throw new IntervalFileSnifferException("Bed files must be ASCII");
    }

    if (numColumns() < 3 || numColumns() > 12) {
      throw new IntervalFileSnifferException("Bed files must have 3-12 columns. " + "This file has " + numColumns());
    }

    if (!StringUtils.isNumeric(column(2))) {
      throw new IntervalFileSnifferException("Bed column 2 must be an integer chromosome coordinate.");
    }

    if (!StringUtils.isNumeric(column(3))) {
      throw new IntervalFileSnifferException("Bed column 3 must be an integer chromosome coordinate.");
    }

    if (isBedGraph()) {
      throw new IntervalFileSnifferException("Bed file appears to be BedGraph format");
    }

    try {
      BedEntry.parse(getFirstLine());
    } catch (Exception e) {
      throw new IntervalFileSnifferException("Error parsing Bed entry: " + e.getMessage());
    }

    return true;
  }

  /**
   * @return true if this file is BedGraph format
   * @throws IOException
   */
  public boolean isBedGraph() throws IOException {
    try {
      return sniffBedGraph();
    } catch (IntervalFileSnifferException e) {
      return false;
    }
  }

  /**
   * Analyze why this is or is not a BedGraph file
   * 
   * @return true if this file is BedGraph format
   * @throws IntervalFileSnifferException
   *           if this is not a BedGraph file
   */
  public boolean sniffBedGraph() throws IOException, IntervalFileSnifferException {
    if (!isAscii()) {
      throw new IntervalFileSnifferException("BedGraph files must be ASCII");
    }

    if (numColumns() != 4) {
      throw new IntervalFileSnifferException("BedGraph files must have 4 columns. " + "This file has " + numColumns());
    }

    if (!StringUtils.isNumeric(column(2))) {
      throw new IntervalFileSnifferException("BedGraph column 2 must be an integer chromosome coordinate.");
    }

    if (!StringUtils.isNumeric(column(3))) {
      throw new IntervalFileSnifferException("BedGraph column 3 must be an integer chromosome coordinate.");
    }

    try {
      Float.parseFloat(column(4));
    } catch (Exception e) {
      throw new IntervalFileSnifferException("BedGraph column 4 must be a float. Cannot parse: " + column(4));
    }

    try {
      BedGraphEntry.parse(getFirstLine());
    } catch (Exception e) {
      throw new IntervalFileSnifferException("Error parsing BedGraph entry: " + e.getMessage());
    }

    return true;
  }

  /**
   * @return true if this file is GFF format
   * @throws IOException
   */
  public boolean isGFF() throws IOException {
    try {
      return sniffGFF();
    } catch (IntervalFileSnifferException e) {
      return false;
    }
  }

  /**
   * Analyze why this is or is not a GFF file
   * 
   * @return true if this file is GFF format
   * @throws IntervalFileSnifferException
   *           if this is not a GFF file
   */
  public boolean sniffGFF() throws IOException, IntervalFileSnifferException {
    if (!isAscii()) {
      throw new IntervalFileSnifferException("GFF files must be ASCII");
    }

    if (numColumns() < 9) {
      throw new IntervalFileSnifferException("GFF files must have at least 9 columns. " + "This file has "
          + numColumns());
    }

    if (!StringUtils.isNumeric(column(4))) {
      throw new IntervalFileSnifferException("BedGraph column 4 must be an integer chromosome coordinate.");
    }

    if (!StringUtils.isNumeric(column(5))) {
      throw new IntervalFileSnifferException("BedGraph column 5 must be an integer chromosome coordinate.");
    }

    try {
      GFFEntry.parse(getFirstLine());
    } catch (Exception e) {
      throw new IntervalFileSnifferException("Error parsing GFF entry: " + e.getMessage());
    }

    return true;
  }

  /**
   * @return true if this file is GeneTrack format (which is detected by the
   *         header line)
   * @throws IOException
   */
  public boolean isGeneTrack() throws IOException {
    try {
      return sniffGeneTrack();
    } catch (IntervalFileSnifferException e) {
      return false;
    }
  }

  /**
   * Analyze why this is or is not a GeneTrack file
   * 
   * @return true if this file is GeneTrack format
   * @throws IntervalFileSnifferException
   *           if this is not a GeneTrack file
   */
  public boolean sniffGeneTrack() throws IOException, IntervalFileSnifferException {
    if (!isAscii()) {
      throw new IntervalFileSnifferException("GeneTrack files must be ASCII");
    }

    if (numColumns() != 4) {
      throw new IntervalFileSnifferException("GeneTrack files must have 4 columns. " + "This file has " + numColumns());
    }

    if (column(1).equalsIgnoreCase("chrom") && column(2).equalsIgnoreCase("index")
        && column(3).equalsIgnoreCase("forward") && column(4).equalsIgnoreCase("reverse")) {
      return true;
    }

    throw new IntervalFileSnifferException("The first line of a GeneTrack file must be: chrom index forward reverse");
  }

  /**
   * @return true if this file is VCF format
   * @throws IOException
   */
  public boolean isVCF() throws IOException {
    try {
      return sniffVCF();
    } catch (IntervalFileSnifferException e) {
      return false;
    }
  }

  /**
   * Analyze why this is or is not a VCF file
   * 
   * @return true if this file is VCF format
   * @throws IntervalFileSnifferException
   *           if this is not a VCF file
   */
  public boolean sniffVCF() throws IOException, IntervalFileSnifferException {
    if (!isAscii()) {
      throw new IntervalFileSnifferException("VCF files must be ASCII");
    }

    if (numColumns() < 8) {
      throw new IntervalFileSnifferException("VCF files must have at least 8 columns. " + "This file has "
          + numColumns());
    }

    if (!StringUtils.isNumeric(column(2))) {
      throw new IntervalFileSnifferException("VCF column 2 must be an integer chromosome coordinate.");
    }

    try {
      VCFEntry.parse(getFirstLine());
    } catch (Exception e) {
      throw new IntervalFileSnifferException("Error parsing VCF entry: " + e.getMessage());
    }

    return true;
  }

  /**
   * @return true if this file is BAM format (with STRICT validation)
   */
  public boolean isBAM() {
    try {
      return sniffBAM();
    } catch (IntervalFileSnifferException e) {
      return false;
    }
  }

  /**
   * Analyze why this is or is not a BAM file
   * 
   * @return true if this file is BAM format
   * @throws IntervalFileSnifferException
   *           if this is not a BAM file
   */
  public boolean sniffBAM() throws IntervalFileSnifferException {
    ValidationStringency stringency = SAMFileReader.getDefaultValidationStringency();
    try (SAMFileReader reader = new SAMFileReader(p.toFile())) {
      SAMFileReader.setDefaultValidationStringency(SAMFileReader.ValidationStringency.DEFAULT_STRINGENCY);
      // Ensure that the first record loads correctly
      SAMRecord r = reader.iterator().next();
      return reader.isBinary();
    } catch (Exception e) {
      throw new IntervalFileSnifferException("Error opening BAM file: " + e.getMessage());
    } finally {
      SAMFileReader.setDefaultValidationStringency(stringency);
    }
  }

  /**
   * @return true if this file is SAM format (with STRICT validation)
   * @throws IOException
   */
  public boolean isSAM() throws IOException {
    try {
      return sniffSAM();
    } catch (IntervalFileSnifferException e) {
      return false;
    }
  }

  /**
   * Analyze why this is or is not a SAM file
   * 
   * @return true if this file is SAM format
   * @throws IntervalFileSnifferException
   *           if this is not a SAM file
   */
  public boolean sniffSAM() throws IOException, IntervalFileSnifferException {
    ValidationStringency stringency = SAMFileReader.getDefaultValidationStringency();
    try (SAMFileReader reader = new SAMFileReader(p.toFile())) {
      SAMFileReader.setDefaultValidationStringency(SAMFileReader.ValidationStringency.DEFAULT_STRINGENCY);
      // Ensure that the first record loads correctly
      SAMRecord r = reader.iterator().next();
      return !reader.isBinary();
    } catch (Exception e) {
      throw new IntervalFileSnifferException("Error opening SAM file: " + e.getMessage());
    } finally {
      SAMFileReader.setDefaultValidationStringency(stringency);
    }
  }

  /**
   * @return the TrackHeader of an ASCII text file, if it exists; null otherwise
   * @throws IOException
   */
  public TrackHeader getTrackHeader() throws IOException {
    if (!isAscii()) {
      return null;
    }

    if (trackHeader == null) {
      String trackLine = null;
      try (BufferedReader reader = Files.newBufferedReader(p, Charset.defaultCharset())) {
        trackLine = reader.readLine();
        if (trackLine.startsWith("track")) {
          trackHeader = TrackHeader.parse(trackLine);
        }
      } catch (TrackHeaderException e) {
        log.warn("Error parsing track line: " + trackLine);
        log.warn("Please email this track line to tim@palpant.us");
      }

      log.debug("Loaded track header of interval file: " + trackHeader);
    }

    return trackHeader;
  }

  /**
   * @return the first non-comment line of an ASCII text file (comments = track,
   *         #, @)
   * @throws IOException
   */
  private String getFirstLine() throws IOException {
    if (!isAscii()) {
      return null;
    }

    if (firstLine == null) {
      try (BufferedReader reader = Files.newBufferedReader(p, Charset.defaultCharset())) {
        firstLine = reader.readLine();

        while (firstLine != null
            && (firstLine.length() == 0 || firstLine.startsWith("track") || firstLine.startsWith("#") || firstLine
                .startsWith("@"))) {
          firstLine = reader.readLine();
        }
      }

      log.debug("Loaded first line of interval file: " + firstLine);
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
   * @param n
   *          the column to return
   * @return the value of the nth column of the first line in this file
   * @throws IOException
   */
  private String column(int n) throws IOException {
    return getFirstLine().split("\t")[n - 1];
  }
}
package edu.unc.genomics.util;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.log4j.Logger;

import net.sf.samtools.BAMIndex;
import net.sf.samtools.BAMIndexer;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecord;

/**
 * Utility methods for calling Picard/Samtools code to work with SAM/BAM files
 * 
 * @author timpalpant
 *
 */
public class Samtools {

  private static final Logger log = Logger.getLogger(Samtools.class);

  /**
   * Convert a SAM file to BAM with default compression Requires that the inputs
   * SAM file have a valid header and be pre-sorted
   * 
   * @param sam
   * @param bam
   */
  public static void samToBam(Path sam, Path bam) {
    log.debug("Converting SAM file " + sam + " to BAM file " + bam);
    final SAMFileReader reader = new SAMFileReader(sam.toFile());
    reader.getFileHeader().setSortOrder(SAMFileHeader.SortOrder.coordinate);
    final SAMFileWriter writer = new SAMFileWriterFactory().makeBAMWriter(reader.getFileHeader(), false, bam.toFile());

    for (SAMRecord samRecord : reader) {
      writer.addAlignment(samRecord);
    }

    writer.close();
    reader.close();
  }

  /**
   * Generates a BAM index file from an input BAM file Adapted from Picard
   * BuildBamIndex
   *
   * @param reader
   *          SAMFileReader for input BAM file
   * @param output
   *          File for output index file
   */
  public static void indexBAMFile(Path bam, Path index) {
    log.debug("Creating BAM index for file: " + bam);
    SAMFileReader reader = new SAMFileReader(bam.toFile());
    BAMIndexer indexer = new BAMIndexer(index.toFile(), reader.getFileHeader());
    reader.enableFileSource(true);

    // Create and write the content
    int count = 0;
    for (SAMRecord rec : reader) {
      indexer.processAlignment(rec);
      count++;
    }

    indexer.finish();
    log.debug("Indexed " + count + " records in BAM file");
  }

  /**
   * Look for BAM index file according to standard naming convention. Adapted
   * from Picard BAMFileReader.java
   *
   * @param dataFile
   *          BAM file name.
   * @return Index file name, or null if not found.
   */
  public static Path findIndexFile(final Path bamFile) {
    // If input is foo.bam, look for foo.bai
    final String bamExtension = ".bam";
    Path indexFile;
    final String fileName = bamFile.toString();
    if (fileName.endsWith(bamExtension)) {
      final String bai = fileName.substring(0, fileName.length() - bamExtension.length()) + BAMIndex.BAMIndexSuffix;
      indexFile = bamFile.resolveSibling(bai);
      if (Files.exists(indexFile)) {
        return indexFile;
      }
    }

    // If foo.bai doesn't exist look for foo.bam.bai
    indexFile = bamFile.resolveSibling(bamFile.getFileName() + ".bai");
    if (Files.exists(indexFile)) {
      return indexFile;
    } else {
      return null;
    }
  }

}

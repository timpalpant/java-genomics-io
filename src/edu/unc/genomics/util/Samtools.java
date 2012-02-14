package edu.unc.genomics.util;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.log4j.Logger;

import net.sf.samtools.BAMIndex;
import net.sf.samtools.BAMIndexer;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

public class Samtools {
	
	private static final Logger log = Logger.getLogger(Samtools.class);
	
  /**
   * Generates a BAM index file from an input BAM file
   * Adapted from Picard BuildBamIndex
   *
   * @param reader SAMFileReader for input BAM file
   * @param output File for output index file
   */
	public static void indexBAMFile(SAMFileReader reader, Path output) {
		BAMIndexer indexer = new BAMIndexer(output.toFile(), reader.getFileHeader());
		reader.enableFileSource(true);
		int totalRecords = 0;

		// Create and write the content
		SAMRecordIterator it = reader.iterator();
		while (it.hasNext()) {
			SAMRecord rec = it.next();
			if (++totalRecords % 1000000 == 0) {
				log.debug(totalRecords + " reads processed ...");
			}
			indexer.processAlignment(rec);
		}
		indexer.finish();
		it.close();
	}
	
	/**
   * Look for BAM index file according to standard naming convention.
   * Adapted from Picard BAMFileReader.java
   *
   * @param dataFile BAM file name.
   * @return Index file name, or null if not found.
   */
	public static Path findIndexFile(final Path bamFile) {
		// If input is foo.bam, look for foo.bai
		final String bamExtension = ".bam";
		Path indexFile;
		final String fileName = bamFile.toString();
		if (fileName.endsWith(bamExtension)) {
			final String bai = fileName.substring(0,
					fileName.length() - bamExtension.length())
					+ BAMIndex.BAMIndexSuffix;
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

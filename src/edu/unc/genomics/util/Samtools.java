package edu.unc.genomics.util;

import java.nio.file.Path;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMException;

public class Samtools {
	public static boolean convertFormat(Path inputFile, Path outputFile, boolean createIndex)
			throws SAMException {
		
    SAMFileReader reader = new SAMFileReader(inputFile.toFile());
    final SAMFileWriter writer = new SAMFileWriterFactory().makeSAMOrBAMWriter(reader.getFileHeader(), true, outputFile.toFile());

    if  (createIndex && writer.getFileHeader().getSortOrder() != SAMFileHeader.SortOrder.coordinate) {
        throw new SAMException("Can't CREATE_INDEX unless sort order is coordinate");
    }
    
    for (SAMRecord alignment : reader) {
        writer.addAlignment(alignment);
    }
    
    reader.close();
    writer.close();
		
		return true;
	}
}

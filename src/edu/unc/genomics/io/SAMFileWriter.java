package edu.unc.genomics.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

import edu.unc.genomics.SAMEntry;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileWriterFactory;

public class SAMFileWriter implements Closeable {
	
	private final Path p;
	
	private final net.sf.samtools.SAMFileWriter writer;
	private final SAMFileHeader header = new SAMFileHeader();
	
	public SAMFileWriter(Path p) {
		this.p = p;
		this.writer = new SAMFileWriterFactory().makeSAMWriter(header, false, p.toFile());
	}
	
	@Override
	public void close() throws IOException {
		writer.close();
	}
	
	public void write(SAMEntry entry) {
		writer.addAlignment(entry.getSAMRecord());
	}
	
	/**
	 * @return the path to this file
	 */
	public Path getPath() {
		return p;
	}
}

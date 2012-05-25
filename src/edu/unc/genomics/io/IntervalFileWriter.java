package edu.unc.genomics.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import edu.unc.genomics.Interval;

/**
 * Base class for writing ASCII text, line-based interval files such as Bed, BedGraph, etc.
 * By default, added Intervals will be output using their toOutput() method
 * If a specific format is desired that is different than the interval object, create a new BedFileWriter, etc.
 * 
 * @author timpalpant
 *
 */
public class IntervalFileWriter<T extends Interval> implements Closeable {

	protected final Path p;
	protected final PrintWriter writer;
	
	/**
	 * Create a new Interval file or append to an existing interval file
	 * @param p the Path to the interval file
	 * @param options if the file should be opened for appending, etc.
	 * @throws IOException if a disk write error occurs
	 */
	public IntervalFileWriter(Path p, OpenOption... options) throws IOException {
		this.p = p;
		writer = new PrintWriter(Files.newBufferedWriter(p, Charset.defaultCharset(), options));
	}

	public void close() {
		writer.close();
	}
	
	/**
	 * Write a comment line to this interval file
	 * NOTE: There is no format checking, so the comment line must be correctly formatted/demarcated
	 * @param line a line to write to this interval file
	 */
	public void writeComment(String line) {
		writer.println(line);
	}

	/**
	 * Write an Interval as line in this output file
	 * @param entry the Interval to write to disk
	 */
	public void write(T entry) {
		writer.println(entry.toOutput());
	}

}

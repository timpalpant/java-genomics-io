package edu.unc.genomics.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import edu.unc.genomics.Interval;

/**
 * Base class for writing ASCII text, line-based interval files such as Bed, BedGraph, etc.
 * @author timpalpant
 *
 * @param <T> the type of entries to write
 */
public abstract class TextIntervalFileWriter<T extends Interval> extends IntervalFileWriter<T> {

	private final PrintWriter writer;
	
	protected TextIntervalFileWriter(Path p, OpenOption... options) throws IOException {
		super(p, options);
		writer = new PrintWriter(Files.newBufferedWriter(p, Charset.defaultCharset(), options));
	}
	
	@Override
	public void close() {
		writer.close();
	}

}

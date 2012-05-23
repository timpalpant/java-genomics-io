package edu.unc.genomics.io;

import java.io.Closeable;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import edu.unc.genomics.Interval;

/**
 * Base class for writing a line-based interval file such as Bed, BedGraph, GFF, etc.
 * 
 * @author timpalpant
 *
 * @param <T> the type of Intervals to write
 */
public abstract class IntervalFileWriter<T extends Interval> implements Closeable {
	
	protected final Path p;
	protected final OpenOption[] options;
	
	protected IntervalFileWriter(Path p, OpenOption... options) {
		this.p = p;
		this.options = options;
	}
	
	public abstract void add(T entry);
	
}

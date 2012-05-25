package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import edu.unc.genomics.Interval;

/**
 * A class for writing BedGraph files to disk
 * @author timpalpant
 *
 * @param <T> the type of intervals that can be written
 */
public class BedGraphFileWriter<T extends Interval> extends IntervalFileWriter<T> {
	
	public BedGraphFileWriter(Path p, OpenOption... options) throws IOException {
		super(p, options);
	}

	@Override
	public void write(T entry) {
		writer.println(entry.toBedGraph());
	}
}

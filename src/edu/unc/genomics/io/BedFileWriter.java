package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import edu.unc.genomics.Interval;

/**
 * A class for writing Bed files to disk
 * @author timpalpant
 *
 */
public class BedFileWriter<T extends Interval> extends IntervalFileWriter<T> {

	public BedFileWriter(Path p, OpenOption... options) throws IOException {
		super(p, options);
	}

	@Override
	public void write(T entry) {
		writer.println(entry.toBed());
	}

}

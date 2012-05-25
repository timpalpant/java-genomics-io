package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import edu.unc.genomics.Interval;

/**
 * A class for writing GFF files to disk
 * @author timpalpant
 *
 */
public class GFFFileWriter<T extends Interval> extends IntervalFileWriter<T> {

	public GFFFileWriter(Path p, OpenOption... options) throws IOException {
		super(p, options);
	}

	@Override
	public void write(T entry) {
		writer.println(entry.toGFF());
	}

}

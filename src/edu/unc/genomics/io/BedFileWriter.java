package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import org.apache.log4j.Logger;

import edu.unc.genomics.Interval;

/**
 * A class for writing Bed files to disk
 * @author timpalpant
 *
 */
public class BedFileWriter<T extends Interval> extends IntervalFileWriter<T> {

	private static final Logger log = Logger.getLogger(BedFileWriter.class);
	
	public BedFileWriter(Path p, OpenOption... options) throws IOException {
		super(p, options);
		log.debug("Opening Bed file writer "+p);
	}

	@Override
	public void write(T entry) {
		write(entry.toBed());
	}

}

package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import org.apache.log4j.Logger;

import edu.unc.genomics.Interval;

/**
 * A class for writing GFF files to disk
 * @author timpalpant
 *
 */
public class GFFFileWriter<T extends Interval> extends IntervalFileWriter<T> {

	private static final Logger log = Logger.getLogger(GFFFileWriter.class);
	
	public GFFFileWriter(Path p, OpenOption... options) throws IOException {
		super(p, options);
		log.debug("Opening GFF file writer "+p);
	}

	@Override
	public void write(T entry) {
		write(entry.toGFF());
	}

}

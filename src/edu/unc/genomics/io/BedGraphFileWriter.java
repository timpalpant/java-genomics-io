package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import org.apache.log4j.Logger;

import edu.unc.genomics.Interval;

/**
 * A class for writing BedGraph files to disk
 * @author timpalpant
 *
 * @param <T> the type of intervals that can be written
 */
public class BedGraphFileWriter<T extends Interval> extends IntervalFileWriter<T> {
	
	private static final Logger log = Logger.getLogger(BedGraphFileWriter.class);
	
	public BedGraphFileWriter(Path p, OpenOption... options) throws IOException {
		super(p, options);
		log.debug("Opening BedGraph file writer "+p);
	}

	@Override
	public synchronized void write(T entry) {
		write(entry.toBedGraph());
	}
}

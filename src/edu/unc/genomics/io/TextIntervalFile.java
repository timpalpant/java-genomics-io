package edu.unc.genomics.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import org.apache.log4j.Logger;

import edu.unc.genomics.Interval;
import edu.unc.genomics.IntervalFactory;

/**
 * @author timpalpant
 *
 */
public abstract class TextIntervalFile<T extends Interval> extends IntervalFile<T> {
	
	private static final Logger log = Logger.getLogger(TextIntervalFile.class);
	
	protected IntervalFactory<T> factory;
	
	protected TextIntervalFile(Path p, IntervalFactory<T> factory) throws IOException {
		super(p);
		this.factory = factory;
	}
	
	@Override
	public void close() {	}

	@Override
	public Iterator<T> iterator() {
		BufferedReader reader = null;
		
		try {
			reader = Files.newBufferedReader(p, Charset.defaultCharset());
		} catch (IOException e) {
			log.error("Error opening BufferedReader for file: " + p.toString());
			e.printStackTrace();
		}
		
		BufferedLineReader lineReader = new BufferedLineReader(reader);
		return new StringIntervalIterator<T>(lineReader.iterator(), factory);
	}

	@Override
	public Iterator<T> query(String chr, int start, int stop) {
		throw new UnsupportedOperationException("Cannot randomly query unindexed TextIntervalFiles");
	}
	
}

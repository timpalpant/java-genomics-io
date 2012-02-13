package edu.unc.genomics.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.unc.genomics.Interval;
import edu.unc.genomics.IntervalFactory;
import edu.unc.genomics.util.FileUtils;

/**
 * @author timpalpant
 *
 */
public abstract class TextIntervalFile<T extends Interval> extends IntervalFile<T> {
	
	private static final Logger log = Logger.getLogger(TextIntervalFile.class);
	
	protected IntervalFactory<T> factory;
	private Set<String> chromosomes;
	private int count = 0;
	
	private final BufferedReader reader;
	private final Iterator<T> iter;
	
	protected TextIntervalFile(Path p, IntervalFactory<T> factory) throws IOException {
		super(p);
		this.factory = factory;
		reader = Files.newBufferedReader(p, Charset.defaultCharset());
		BufferedLineReader lineReader = new BufferedLineReader(reader);
		iter = new StringIntervalIterator<T>(lineReader.iterator(), factory);
	}
	
	@Override
	public void close() throws IOException {	
		reader.close();
	}
	
	public Set<String> chromosomes() {
		if (chromosomes == null) {
			chromosomes = new HashSet<String>();
			count = 0;
			for (Interval i : this) {
				chromosomes.add(i.getChr());
				count++;
			}
		}
		
		return chromosomes;
	}
	
	public int count() {
		if (count == 0) {
			try {
				count = FileUtils.countLines(p);
			} catch (IOException e) {
				log.error("Error counting lines in file: " + p);
				e.printStackTrace();
			}
		}
		
		return count;
	}

	@Override
	public Iterator<T> iterator() {
		return iter;
	}

	@Override
	public Iterator<T> query(String chr, int start, int stop) {
		throw new UnsupportedOperationException("Cannot randomly query unindexed TextIntervalFiles");
	}
	
}

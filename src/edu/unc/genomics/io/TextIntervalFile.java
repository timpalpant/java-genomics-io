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

/**
 * @author timpalpant
 *
 */
public abstract class TextIntervalFile<T extends Interval> extends IntervalFile<T> {
	
	private static final Logger log = Logger.getLogger(TextIntervalFile.class);
	
	protected IntervalFactory<T> factory;
	private Set<String> chromosomes;
	private int count = 0;
	
	protected TextIntervalFile(Path p, IntervalFactory<T> factory) throws IOException {
		super(p);
		this.factory = factory;
	}
	
	@Override
	public void close() {	}
	
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
			for (Interval i : this) {
				count++;
			}
		}
		
		return count;
	}

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
		//throw new UnsupportedOperationException("Cannot randomly query unindexed TextIntervalFiles");
		
		log.warn("Random queries against text files will result in poor performance. Indexing with Tabix is recommended.");
		BufferedReader reader = null;
		
		try {
			reader = Files.newBufferedReader(p, Charset.defaultCharset());
		} catch (IOException e) {
			log.error("Error opening BufferedReader for file: " + p.toString());
			e.printStackTrace();
		}
		
		BufferedLineReader lineReader = new BufferedLineReader(reader);
		return new SelectiveStringIntervalIterator<T>(lineReader.iterator(), factory, chr, start, stop);
	}
	
}

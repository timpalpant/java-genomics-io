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
	
	private Path bgzip;
	private Path tabix;
	private TabixFile<T> tabixFile;
	
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
		
		if (bgzip != null) {
			Files.deleteIfExists(bgzip);
		}
		
		if (tabix != null) {
			Files.deleteIfExists(tabix);
		}
	}
	
	public Set<String> chromosomes() {
		if (chromosomes == null) {
			// If we've already indexed for Tabix, get the chromosomes from there
			if (tabixFile != null) {
				chromosomes = tabixFile.chromosomes();
			} else {
				chromosomes = new HashSet<String>();
				count = 0;
				for (Interval i : this) {
					chromosomes.add(i.getChr());
					count++;
				}
			}
		}
		
		return chromosomes;
	}
	
	public int count() {
		if (count == 0) {
			if (tabixFile != null) {
				count = tabixFile.count();
			} else {
				try {
					count = FileUtils.countLines(p);
				} catch (IOException e) {
					log.error("Error counting lines in file: " + p);
					throw new RuntimeException("Error counting lines in file");
				}
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
		// Index the file with Tabix to enable querying
		if (tabixFile == null) {
			convertToTabix();
		}
		
		return tabixFile.query(chr, start, stop);
	}
	
	private void convertToTabix() {
		// Sort the input file
		
		// BGZip the sorted file
		
		// Delete the sorted (uncompressed) file
		
		// Index with Tabix
	}
	
}

package edu.unc.genomics.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sf.samtools.TabixWriter.TabixException;

import org.apache.log4j.Logger;

import edu.unc.genomics.Interval;
import edu.unc.genomics.IntervalFactory;
import edu.unc.genomics.util.FileUtils;
import edu.unc.genomics.util.Tabix;

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
	private Path index;
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
		
		if (index != null) {
			Files.deleteIfExists(index);
		}
	}
	
	public Set<String> chromosomes() {
		if (chromosomes == null) {
			if (tabixFile == null) {
				convertToTabix();
			}
			
			chromosomes = tabixFile.chromosomes();
		}
		
		return chromosomes;
	}
	
	public int count() {
		if (count == 0) {
			if (tabixFile == null) {
				convertToTabix();
			}
			
			count = tabixFile.count();
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
		try {
			Path sorted = Files.createTempFile(p.getFileName().toString(), ".sorted");
			log.debug("Sorting interval file to temp: " + sorted);
			sorted.toFile().deleteOnExit();
			Tabix.sortFile(p, sorted, factory.tabixConf());
			
			// BGZip the sorted file
			bgzip = p.resolveSibling(sorted.getFileName()+".gz");
			bgzip.toFile().deleteOnExit();
			
			Tabix.bgzip(sorted, bgzip);
			
			index = Tabix.index(bgzip, factory.tabixConf());
			index.toFile().deleteOnExit();
		} catch (IOException e3) {
			log.error("Error sorting and compressing interval file");
			throw new RuntimeException("Error sorting and compressing interval file");
		} catch (TabixException e1) {
			log.error("Error indexing with Tabix");
			e1.printStackTrace();
			throw new RuntimeException("Error indexing Tabix file");
		}
		
		try {
			tabixFile = new TabixFile<T>(bgzip, factory);
		} catch (IOException e) {
			log.error("Error initializing Tabix file");
			e.printStackTrace();
			throw new RuntimeException("Error initializing Tabix file");
		}
	}
	
}

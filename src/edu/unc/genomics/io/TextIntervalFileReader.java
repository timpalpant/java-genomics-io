package edu.unc.genomics.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import net.sf.samtools.TabixWriter.Conf;
import net.sf.samtools.TabixWriter.TabixException;

import org.apache.log4j.Logger;

import edu.unc.genomics.Interval;
import edu.unc.genomics.IntervalFactory;
import edu.unc.genomics.util.FileUtils;
import edu.unc.genomics.util.Tabix;

/**
 * Base class for all ASCII-text, line-based, tab-delimited interval files, such as Bed, BedGraph, GFF, etc.
 * 
 * @author timpalpant
 *
 */
public abstract class TextIntervalFileReader<T extends Interval> extends IntervalFileReader<T> {
	
	private static final Logger log = Logger.getLogger(TextIntervalFileReader.class);
	
	protected IntervalFactory<T> factory;
	private Set<String> chromosomes;
	private int count = 0;
	
	private final BufferedReader reader;
	private final Iterator<T> iter;
	
	private Path bgzip;
	private Path index;
	private TabixFileReader<T> tabixFile;
	
	protected TextIntervalFileReader(Path p, IntervalFactory<T> factory) throws IOException {
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
	
	@Override
	public Set<String> chromosomes() {
		if (chromosomes == null) {
			if (tabixFile == null) {
				convertToTabix();
			}
			
			chromosomes = tabixFile.chromosomes();
		}
		
		return chromosomes;
	}
	
	@Override
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
	
	/**
	 * Filter, sort, BGZip, and index this file with Tabix for random querying
	 */
	private void convertToTabix() {
		try {
			// Filter the input file
			Path filtered = Files.createTempFile(p.getFileName().toString(), ".filtered");
			filtered.toFile().deleteOnExit();
			log.debug("Filtering interval file to temp: " + filtered);
			try (BufferedWriter writer = Files.newBufferedWriter(filtered, Charset.defaultCharset());
					BufferedReader reader = Files.newBufferedReader(p, Charset.defaultCharset())) {
				String line;
				T interval;
				while ((line = reader.readLine()) != null) {
					interval = factory.parse(line);
					// This will filter out comment lines and invalid lines
					if (interval != null) {
						writer.write(line);
						writer.newLine();
					}
				}
			}
			
			// Sort the input file
			Path sorted = Files.createTempFile(p.getFileName().toString(), ".sorted");
			log.debug("Sorting interval file to temp: " + sorted);
			sorted.toFile().deleteOnExit();
			FileUtils.sort(filtered, sorted, getTabixComparator());
			
			// BGZip the sorted file
			bgzip = p.resolveSibling(sorted.getFileName()+".gz");
			bgzip.toFile().deleteOnExit();
			Tabix.bgzip(sorted, bgzip);
			
			// Index the BGZipped file with Tabix
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
		
		// Open the file with a new TabixFile reader
		try {
			tabixFile = new TabixFileReader<T>(bgzip, factory);
		} catch (IOException e) {
			log.error("Error initializing Tabix file");
			e.printStackTrace();
			throw new RuntimeException("Error initializing Tabix file");
		}
	}
	
	/** Returns a comparator that will sort two entries by genomic location
	 * @return a new genomic locus comparator
	 */
	protected Comparator<String> getTabixComparator() {
		// Make a new comparator that will sort the file by genomic location
		Comparator<String> comparator = new Comparator<String>() {
			public int compare(final String s1, final String s2) {
				Conf conf = factory.tabixConf();
				
				// Parse the two lines into intervals
				String[] entry1 = s1.split("\t");
				String[] entry2 = s2.split("\t");
				
				// First sort by chromosome
				String chr1 = entry1[conf.chrColumn-1];
				String chr2 = entry2[conf.chrColumn-1];
				int c1 = chr1.compareTo(chr2);
				if (c1 != 0) {
					return c1;
				}
				
				// Then sort by start
				Integer start1 = Integer.valueOf(entry1[conf.startColumn-1]);
				Integer start2 = Integer.valueOf(entry2[conf.startColumn-1]);
				int c2 = start1.compareTo(start2);
				if (c2 != 0) {
					return c2;
				}
				
				// Then sort by end
				Integer stop1 = Integer.valueOf(entry1[conf.endColumn-1]);
				Integer stop2 = Integer.valueOf(entry2[conf.endColumn-1]);
				int c3 = stop1.compareTo(stop2);
				
				// If they are still equal at this point, then they are equal
				return c3;
			}
		};
		
		return comparator;
	}
	
}

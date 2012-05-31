package edu.unc.genomics;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.DataFormatException;

import org.apache.log4j.Logger;

/**
 * Holds chromosome length information loaded from *.len files
 * Assembly files should be tab-delimited, with the format "chrIV\t123123"
 * where chrIV is the chromosome name and 123123 is the length of chrIV
 * 
 * @author timpalpant
 *
 */
public class Assembly implements Iterable<String> {
	
	private static final Logger log = Logger.getLogger(Assembly.class);
	
	private final Path p;
	private final Map<String, Integer> index = new HashMap<String, Integer>();
	
	/**
	 * Initialize a new Assembly from the *.len file in p
	 * @param p the path to the *.len file
	 * @throws IOException if an IOException occurs while reading from p
	 * @throws DataFormatException if the *.len file is not formatted correctly
	 */
	public Assembly(Path p) throws IOException, DataFormatException {
		this.p = p;
		log.debug("Loading assembly "+this);
		try (BufferedReader reader = Files.newBufferedReader(p, Charset.defaultCharset())) {
			String line;
			while ((line = reader.readLine()) != null) {
				int delim = line.indexOf('\t');
				if (delim == -1) {
					throw new DataFormatException("Invalid format in Assembly file");
				}
				
				try {
					String chr = line.substring(0, delim);
					Integer length = Integer.valueOf(line.substring(delim+1));
					index.put(chr, length);
				} catch (NumberFormatException e) {
					throw new DataFormatException("Invalid format in Assembly file");
				}
			}
		}
		log.debug("Loaded "+index.size()+" chromosomes from assembly");
	}
	
	public final Path getPath() {
		return p;
	}
	
	@Override
	public final String toString() {
		String name = p.getFileName().toString();
		if (name.endsWith(".len")) {
			name = name.substring(0, name.length()-4);
		}
		return name;
	}
	
	/**
	 * The set of chromosomes in this Assembly
	 * @return all chromosomes in this Assembly
	 */
	public final Set<String> chromosomes() {
		return index.keySet();
	}
	
	/**
	 * Does this Assembly contain a specific chromosome?
	 * @param chr the chromosome to look for
	 * @return true if this Assembly contains chr, false otherwise
	 */
	public final boolean includes(String chr) {
		return index.containsKey(chr);
	}
	
	/**
	 * Get the length of a chromosome in this Assembly
	 * @param chr the chromosome to look for
	 * @return the length of chr
	 */
	public final Integer getChrLength(String chr) {
		return index.get(chr);
	}

	@Override
	public final Iterator<String> iterator() {
		return index.keySet().iterator();
	}
}

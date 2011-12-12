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

public class Assembly implements Iterable<String> {
	private final Path p;
	private final Map<String, Integer> index = new HashMap<String, Integer>();
	
	public Assembly(Path p) throws IOException, DataFormatException {
		this.p = p;
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
	}
	
	public Path getPath() {
		return p;
	}
	
	@Override
	public String toString() {
		String name = p.getFileName().toString();
		if (name.endsWith(".len")) {
			name = name.substring(0, name.length()-4);
		}
		return name;
	}
	
	public Set<String> chromosomes() {
		return index.keySet();
	}
	
	public boolean includes(String chr) {
		return index.containsKey(chr);
	}
	
	public Integer getChrLength(String chr) {
		return index.get(chr);
	}

	@Override
	public Iterator<String> iterator() {
		return index.keySet().iterator();
	}
}

package edu.unc.genomics.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import org.apache.log4j.Logger;

import edu.unc.genomics.FastqEntry;

public class FastqFile extends SequenceFile<FastqEntry> {

	private static final Logger log = Logger.getLogger(FastqFile.class);
	private final BufferedReader reader;
	private final FastqIterator iter;
	
	protected FastqFile(Path p) throws IOException {
		super(p);
		reader = Files.newBufferedReader(p, Charset.defaultCharset());
		iter = new FastqIterator();
	}

	@Override
	public Iterator<FastqEntry> iterator() {
		return iter;
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}
	
	private class FastqIterator implements Iterator<FastqEntry> {
		
		private FastqEntry next;
		
		private FastqIterator() throws IOException {
			advance();
		}
		
		@Override
		public boolean hasNext() {
			return (next != null);
		}

		@Override
		public FastqEntry next() {
			FastqEntry entry = next;
			try {
				advance();
			} catch (IOException e) {
				log.error("Error reading from FASTQ file");
				e.printStackTrace();
			}
			return entry;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Cannot delete entries in FASTQ file");
		}
		
		private void advance() throws IOException {
			String id = reader.readLine();
			String seq = reader.readLine();
			String line3 = reader.readLine();
			String qual = reader.readLine();
			
			if (id != null && seq != null && qual != null) {
				if (!id.startsWith("@") || !line3.startsWith("+")) {
					throw new SequenceFileFormatException("Invalid format in FASTQ file: " + p);
				}
				id = id.substring(1);
				next = new FastqEntry(id, seq, qual);
			} else {
				next = null;
			}
		}
		
	}

}

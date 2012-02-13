package edu.unc.genomics.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import org.apache.log4j.Logger;

import edu.unc.genomics.FastaEntry;

public class FastaFile extends SequenceFile<FastaEntry> {

	private static final Logger log = Logger.getLogger(FastaFile.class);
	private final BufferedReader reader;
	private final FastaIterator iter;
	
	protected FastaFile(Path p) throws IOException {
		super(p);
		reader = Files.newBufferedReader(p, Charset.defaultCharset());
		iter = new FastaIterator();
	}

	@Override
	public Iterator<FastaEntry> iterator() {
		return iter;
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}
	
	private class FastaIterator implements Iterator<FastaEntry> {
		
		private String nextLine;
		
		private FastaIterator() {
			try {
				nextLine = reader.readLine();
			} catch (IOException e) {
				log.error("Error reading from FASTA file");
				e.printStackTrace();
			}
		}
		
		@Override
		public boolean hasNext() {
			return (nextLine != null);
		}

		@Override
		public FastaEntry next() {
			if (!nextLine.startsWith(">")) {
				throw new SequenceFileFormatException("Invalid format in FASTA file: " + p);
			}
			
			String id = nextLine.substring(1);
			StringBuilder seq = new StringBuilder();
			try {
				while ((nextLine = reader.readLine()) != null && !nextLine.startsWith(">")) {
					seq.append(nextLine);
				}
			} catch (IOException e) {
				log.error("Error reading from Fasta file");
				throw new SequenceFileFormatException("Invalid format in FASTA file: " + p);
			}
			
			return new FastaEntry(id, seq.toString());
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Cannot delete entries in FASTA file");
		}
		
	}

}

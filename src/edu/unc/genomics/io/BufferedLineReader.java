package edu.unc.genomics.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import edu.unc.genomics.io.LineReader;

/**
 * A BufferedReader that implements LineReader
 * 
 * @author timpalpant
 *
 */
public class BufferedLineReader implements LineReader, Iterable<String>, Closeable {

	private BufferedReader reader;
	
	/**
	 * @param in
	 */
	public BufferedLineReader(BufferedReader reader) {
		this.reader = reader;
	}
	
	@Override
	public void close() throws IOException {
		reader.close();
	}

	@Override
	public String readLine() throws IOException {
		return reader.readLine();
	}

	@Override
	public Iterator<String> iterator() {
		return new LineReaderIterator(this);
	}

}

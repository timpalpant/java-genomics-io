package edu.unc.genomics.io;

import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * @author timpalpant
 *
 */
public class LineReaderIterator implements Iterator<String> {
	
	private static Logger log = Logger.getLogger(LineReaderIterator.class);

	private final LineReader reader;
	private String nextLine;
	
	public LineReaderIterator(LineReader reader) {
		this.reader = reader;
		advance();
	}
	
	@Override
	public boolean hasNext() {
		return (nextLine == null);
	}

	@Override
	public String next() {
		String line = nextLine;
		advance();
		return line;
	}

	@Override
	public void remove() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Cannot remove records from a BufferedReaderIterator");
	}
	
	private void advance() {
		try {
			nextLine = reader.readLine();
		} catch (IOException e) {
			log.error("Error getting next line from BufferedReader");
			e.printStackTrace();
		}
	}

}

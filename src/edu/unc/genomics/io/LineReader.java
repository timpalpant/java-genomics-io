package edu.unc.genomics.io;

import java.io.IOException;

/**
 * A Reader that allows reading one line at a time
 * 
 * @author timpalpant
 *
 */
public interface LineReader {
	String readLine() throws IOException;
}

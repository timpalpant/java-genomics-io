/**
 * 
 */
package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Path;

import edu.unc.genomics.GFFEntry;
import edu.unc.genomics.IntervalFactory;

/**
 * @author timpalpant
 *
 */
public class GFFFile extends TextIntervalFile<GFFEntry> {

	public GFFFile(Path p) throws IOException {
		super(p, new GFFEntryFactory());
	}
	
	public static class GFFEntryFactory implements IntervalFactory<GFFEntry> {
		
		@Override
		public GFFEntry parse(String line) {
			return GFFEntry.parse(line);
		}

	}

}

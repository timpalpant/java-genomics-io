/**
 * 
 */
package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Path;

import net.sf.samtools.TabixWriter;
import net.sf.samtools.TabixWriter.Conf;

import edu.unc.genomics.BedEntry;
import edu.unc.genomics.IntervalFactory;

/**
 * @author timpalpant
 *
 */
public class BedFile extends TextIntervalFile<BedEntry> {

	public BedFile(Path p) throws IOException {
		super(p, new BedEntryFactory());
	}
	
	public static class BedEntryFactory implements IntervalFactory<BedEntry> {
		
		@Override
		public BedEntry parse(String line) {
			return BedEntry.parse(line);
		}

		@Override
		public Conf tabixConf() {
			return TabixWriter.BED_CONF;
		}

	}

}

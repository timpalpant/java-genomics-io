package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Path;

import net.sf.samtools.TabixWriter;
import net.sf.samtools.TabixWriter.Conf;

import edu.unc.genomics.BedEntry;
import edu.unc.genomics.IntervalFactory;

/**
 * A Bed file. For the format, see http://genome.ucsc.edu/FAQ/FAQformat
 * 
 * @author timpalpant
 *
 */
public class BedFileReader extends TextIntervalFileReader<BedEntry> {

	public BedFileReader(Path p) throws IOException {
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

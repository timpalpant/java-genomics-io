package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.log4j.Logger;

import net.sf.samtools.TabixWriter;
import net.sf.samtools.TabixWriter.Conf;

import edu.unc.genomics.BedGraphEntry;
import edu.unc.genomics.IntervalFactory;

/**
 * A BedGraph file. For the format see http://genome.ucsc.edu/FAQ/FAQformat
 * 
 * @author timpalpant
 *
 */
public class BedGraphFileReader extends TextIntervalFileReader<BedGraphEntry> {

	private static final Logger log = Logger.getLogger(BedGraphFileReader.class);
	
	public BedGraphFileReader(Path p) throws IOException {
		super(p, new BedGraphEntryFactory());
		log.debug("Opening BedGraph file reader "+p);
	}

	public static class BedGraphEntryFactory implements IntervalFactory<BedGraphEntry> {

		@Override
		public BedGraphEntry parse(String line) {
			return BedGraphEntry.parse(line);
		}

		@Override
		public Conf tabixConf() {
			return TabixWriter.BED_CONF;
		}
		
	}
}

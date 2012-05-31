package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.log4j.Logger;

import net.sf.samtools.TabixWriter;
import net.sf.samtools.TabixWriter.Conf;

import edu.unc.genomics.GFFEntry;
import edu.unc.genomics.IntervalFactory;

/**
 * A GFF file. For more information, see: http://genome.ucsc.edu/FAQ/FAQformat
 * @author timpalpant
 *
 */
public class GFFFileReader extends TextIntervalFileReader<GFFEntry> {

	private static final Logger log = Logger.getLogger(GFFFileReader.class);
	
	public GFFFileReader(Path p) throws IOException {
		super(p, new GFFEntryFactory());
		log.debug("Opening GFF file reader "+p);
	}
	
	public static class GFFEntryFactory implements IntervalFactory<GFFEntry> {
		
		@Override
		public GFFEntry parse(String line) {
			return GFFEntry.parse(line);
		}

		@Override
		public Conf tabixConf() {
			return TabixWriter.GFF_CONF;
		}

	}

}

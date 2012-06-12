package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.log4j.Logger;

import net.sf.samtools.TabixWriter;
import net.sf.samtools.TabixWriter.Conf;

import edu.unc.genomics.IntervalFactory;
import edu.unc.genomics.VCFEntry;

/**
 * A VCF file. For the format, see http://www.1000genomes.org/wiki/Analysis/Variant%20Call%20Format/vcf-variant-call-format-version-41
 * 
 * @author timpalpant
 *
 */
public class VCFFileReader extends TextIntervalFileReader<VCFEntry> {

	private static final Logger log = Logger.getLogger(VCFFileReader.class);
	
	public VCFFileReader(Path p) throws IOException {
		super(p, new VCFEntryFactory());
		log.debug("Opening VCF file reader "+p);
	}
	
	public static class VCFEntryFactory implements IntervalFactory<VCFEntry> {
		
		@Override
		public VCFEntry parse(String line) {
			return VCFEntry.parse(line);
		}

		@Override
		public Conf tabixConf() {
			return TabixWriter.VCF_CONF;
		}

	}

}

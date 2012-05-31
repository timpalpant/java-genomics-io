package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import net.sf.samtools.BAMIndex;
import net.sf.samtools.BAMIndexMetaData;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;

import edu.unc.genomics.SAMEntry;
import edu.unc.genomics.util.Samtools;

/**
 * Text SAM files. Will passively convert to BAM and index if random queries are attempted.
 * For more information about SAM files, see: http://samtools.sourceforge.net
 * 
 * @author timpalpant
 *
 */
public class SAMFileReader extends IntervalFileReader<SAMEntry> {
	
	private static final Logger log = Logger.getLogger(SAMFileReader.class);
	
	private net.sf.samtools.SAMFileReader reader;
	private SAMRecordIterator it;
	private Set<String> chromosomes;
	private int count = 0;
	
	private Path bam;
	private Path index;
	
	/**
	 * By default, consider all alignments from iterators for analyses (mapped or unmapped)
	 */
	private boolean allowUnmappedReads = true;
	
	public SAMFileReader(Path p) {
		super(p);
		log.debug("Opening SAM file reader "+p);
		reader = new net.sf.samtools.SAMFileReader(p.toFile());
	}
	
	public SAMFileReader(Path p, boolean allowUnmappedReads) {
		this(p);
		this.allowUnmappedReads = allowUnmappedReads;
	}
	
	@Override
	public void close() throws IOException {
		log.debug("Closing SAM file reader "+p);
		reader.close();
	}
	
	@Override
	public int count() {
		if (count == 0) {
			if (bam == null) {
				convertToBAM();
			}

			BAMIndex index = reader.getIndex();
	    int nRefs = reader.getFileHeader().getSequenceDictionary().size();
	    for (int i = 0; i < nRefs; i++) {
	    	BAMIndexMetaData data = index.getMetaData(i);
	    	count += data.getAlignedRecordCount();
	    	count += data.getUnalignedRecordCount();
	    }
		}
		
		return count;
	}

	@Override
	public Set<String> chromosomes() {
		if (chromosomes == null) {
			chromosomes = new LinkedHashSet<String>();
			SAMSequenceDictionary dict = reader.getFileHeader().getSequenceDictionary();
			for (SAMSequenceRecord seqRec : dict.getSequences()) {
				chromosomes.add(seqRec.getSequenceName());
			}
		}
		
		return chromosomes;
	}

	@Override
	public Iterator<SAMEntry> iterator() {
		// Close any previous iterators since SAM-JDK only allows one at a time
		if (it != null) {
			it.close();
		}

		it = reader.iterator();
		return new SAMEntryIterator(it, allowUnmappedReads);
	}

	@Override
	public Iterator<SAMEntry> query(String chr, int start, int stop) {
		if (bam == null) {
			convertToBAM();
		}
		
		// Close any previous iterators since SAM-JDK only allows one at a time
		if (it != null) {
			it.close();
		}

		it = reader.query(chr, start, stop, false);
		return new SAMEntryIterator(it, allowUnmappedReads);
	}
	
	private void convertToBAM() {
		// Convert the SAM file to BAM to do queries
		log.debug("Autoconverting SAM file ("+p+") to BAM");
		try {
			bam = Files.createTempFile(p.getFileName().toString(), ".bam");
		} catch (IOException e) {
			log.error("Error creating temporary BAM file from SAM: " + p.getFileName());
			e.printStackTrace();
			throw new RuntimeException("Error creating temporary BAM file from SAM: " + p.getFileName());
		}
		// Hook for automatically deleting the BAM file when the JVM terminates
		bam.toFile().deleteOnExit();
		Samtools.samToBam(p, bam);
		
		// Index the BAM file
		index = bam.resolveSibling(bam.getFileName()+BAMIndex.BAMIndexSuffix);
		index.toFile().deleteOnExit();
		Samtools.indexBAMFile(bam, index);
				
		reader = new net.sf.samtools.SAMFileReader(bam.toFile(), index.toFile());
		// Ensure that we have an index
		if (!reader.hasIndex()) {
			throw new IntervalFileFormatException("Error indexing BAM file: "+bam);
		}
			
		// Turn off memory mapping to avoid BufferUnderRun exceptions
		reader.enableIndexMemoryMapping(false);
		// Turn on index caching
		reader.enableIndexCaching(true);
	}

	/**
	 * @return the allowUnmappedReads
	 */
	public boolean doesAllowUnmappedReads() {
		return allowUnmappedReads;
	}

	/**
	 * @param allowUnmappedReads the allowUnmappedReads to set
	 */
	public void setAllowUnmappedReads(boolean allowUnmappedReads) {
		this.allowUnmappedReads = allowUnmappedReads;
	}
}

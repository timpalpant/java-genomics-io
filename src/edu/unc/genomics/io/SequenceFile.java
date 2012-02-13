package edu.unc.genomics.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.unc.genomics.Sequence;

public abstract class SequenceFile<T extends Sequence> implements Iterable<T>, Closeable {
	
	private static final Logger log = Logger.getLogger(SequenceFile.class);
	
	protected final Path p;
	
	protected SequenceFile(Path p) {
		this.p = p;
	}
	
	public static SequenceFile<? extends Sequence> autodetect(Path p) throws SequenceFileSnifferException, IOException {
		SequenceFileSniffer sniffer = new SequenceFileSniffer(p);
		
		if (sniffer.isFasta()) {
			log.info("Autodetected FASTA filetype for: " + p.getFileName().toString());
			return new FastaFile(p);
		} else if (sniffer.isFastq()) {
			log.info("Autodetected FASTQ filetype for: " + p.getFileName().toString());
			return new FastqFile(p);
		} else if (sniffer.isTwoBit()) {
			throw new SequenceFileSnifferException("TwoBit files are not yet supported");
		} else {
			throw new SequenceFileSnifferException("Could not autodetect sequence file format");
		}
	}
	
	public static List<Sequence> loadAll(Path p) throws SequenceFileSnifferException, IOException {
		List<Sequence> all = new ArrayList<>();
		try (SequenceFile<? extends Sequence> input = SequenceFile.autodetect(p)) {
			for (Sequence s : input) {
				all.add(s);
			}
		}
		
		return all;
	}
	
	public List<T> loadAll() {
		List<T> all = new ArrayList<>();
		for (T entry : this) {
			all.add(entry);
		}
		return all;
	}
	
	public Path getPath() {
		return p;
	}
}

package edu.unc.genomics.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.log4j.Logger;

import edu.unc.genomics.util.FileUtils;

public class SequenceFileSniffer {
	
	private static final Logger log = Logger.getLogger(SequenceFileSniffer.class);
	
	protected Path p;
	
	public SequenceFileSniffer(Path p) {
		this.p = p;
	}
	
	public boolean isFasta() throws IOException {
		if (!FileUtils.isAsciiText(p)) { return false; }
		String line1;
		try (BufferedReader reader = Files.newBufferedReader(p, Charset.defaultCharset())) {
			line1 = reader.readLine();
		}
		
		if (line1 == null) {
			return false;
		}
		
		// TODO Better FASTA format checking
		return line1.startsWith(">");
	}
	
	public boolean isFastq() throws IOException {
		if (!FileUtils.isAsciiText(p)) { return false; }
		String line1, line3;
		try (BufferedReader reader = Files.newBufferedReader(p, Charset.defaultCharset())) {
			line1 = reader.readLine();
			reader.readLine();
			line3 = reader.readLine();
		}
		
		if (line1 == null || line3 == null) {
			return false;
		}
		
		// TODO Better FASTQ format checking
		return line1.startsWith("@") && line3.startsWith("+");
	}
	
	public boolean isTwoBit() throws IOException {
		if (!FileUtils.isAsciiText(p)) { return true; }
		
		// TODO Better TwoBit format checking
		return false;
	}
}

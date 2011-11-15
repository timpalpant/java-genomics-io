package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import edu.ucsc.genome.TrackHeader;
import edu.ucsc.genome.TrackHeaderException;

public class TextWigFile extends WigFile {
	private FileChannel fc;
	private Scanner scanner;
	private TrackHeader header;
	private WigIndex index;

	public TextWigFile(Path p) throws IOException {
		super(p);
		
		fc = (FileChannel)Files.newByteChannel(p);
		scanner = new Scanner(fc);
		String headerLine = scanner.nextLine();
		if (headerLine.startsWith("track")) {
			try {
				header = TrackHeader.parse(headerLine);
			} catch (TrackHeaderException e) {
				System.err.println("Error parsing UCSC track header in file: " + p.toString());
				System.err.println(e.getMessage());
			}
		}
	}

	@Override
	public double[] query(String chr, int start, int stop) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> chromosomes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getChrStart(String chr) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getChrStop(String chr) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean includes(String chr, int start, int stop) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean includes(String chr) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long length() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long numBases() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double total() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double mean() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double stdev() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double min() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double max() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

}

package edu.unc.genomics.io;

import java.io.IOException;
import java.util.Iterator;

import ed.javatools.BufferedRandomAccessFile;
import edu.unc.genomics.Contig;
import edu.unc.genomics.Interval;
import edu.unc.genomics.WigEntry;

/**
 * Holds index information about variableStep contigs in a TextWigFile
 * @author timpalpant
 *
 */
class VariableStepContigIndex extends ContigIndex {
	
	private static final long serialVersionUID = 3139905829545756903L;

	public VariableStepContigIndex(String chr, int start, int stop, int span) {
		super(chr, start, stop, span);
	}
	
	public static VariableStepContigIndex parseHeader(String headerLine) throws WigFileFormatException {
		String[] tokens = headerLine.split(" ");
		if (tokens.length == 0 || !tokens[0].equals(Contig.Type.VARIABLESTEP.getId())) {
			throw new WigFileFormatException("Not a valid variableStep header line: " + headerLine);
		}
		
		String chr = "";
		int start = 1;
		int span = 1;
		for (int i = 1; i < tokens.length; i++ ) {
			String s = tokens[i];
			String[] pair = s.split("=");
			if (pair.length != 2) {
				throw new WigFileFormatException("Invalid keypair in variableStep header line: " + s);
			}
			
			String key = pair[0];
			String value = pair[1];
			switch(key) {
			case "chrom":
				chr = value;
				break;
			case "span":
				span = Integer.parseInt(value);
				break;
			default:
				throw new WigFileFormatException("Invalid attribute in variableStep header line: " + key);
			}
		}
		
		return new VariableStepContigIndex(chr, start, -1, span);
	}
	
	@Override
	public String toOutput() {
		return Contig.Type.VARIABLESTEP.getId() + " chrom=" + getChr() + " span=" + getSpan();
	}

	@Override
	public VariableStepContigIterator query(BufferedRandomAccessFile raf, Interval interval) throws IOException, WigFileException {
		return new VariableStepContigIterator(raf, interval);
	}
	
	private class VariableStepContigIterator implements Iterator<WigEntry> {

		private final BufferedRandomAccessFile raf;
		private final int low;
		private final int high;

		private int bp;
		private float value;
		private boolean hasNextLine = false;
		
		public VariableStepContigIterator(final BufferedRandomAccessFile raf, final Interval interval) throws IOException, WigFileException {
			this.raf = raf;
			
			// Clamp to bases that are covered by this Contig
			low = Math.max(start, interval.low());
			high = Math.min(stop, interval.high());
			
			// Find the closest known upstream base-pair position
			int closestUpstream = getUpstreamIndexedBP(low);
			raf.seek(getIndex(closestUpstream));
			
			// Move to the first item
			String line;
			while ((line = raf.readLine2()) != null) {
				// Break if at the next Contig
				if (line.startsWith(Contig.Type.FIXEDSTEP.getId()) || line.startsWith(Contig.Type.VARIABLESTEP.getId())) {
					hasNextLine = false;
					break;
				}
				
				int delim = line.indexOf('\t');
				bp = Integer.parseInt(line.substring(0, delim));
				if (bp + getSpan() - 1 >= low) {
					value = Float.parseFloat(line.substring(delim+1));
					hasNextLine = true;
					break;
				}
			}
		}
		
		@Override
		public boolean hasNext() {
			return hasNextLine && bp <= high;
		}

		@Override
		public WigEntry next() {
			WigEntry item = new WigEntry(chr, bp, bp+getSpan()-1, value);
			
			try {
				String line = raf.readLine2();
				if (line == null || line.startsWith(Contig.Type.FIXEDSTEP.getId()) || line.startsWith(Contig.Type.VARIABLESTEP.getId())) {
					hasNextLine = false;
				} else {
					int delim = line.indexOf('\t');
					bp = Integer.parseInt(line.substring(0, delim));
					value = Float.parseFloat(line.substring(delim+1));
					hasNextLine = true;
				}
			} catch (IOException e) {
				throw new RuntimeException("Error getting next item from Wig file");
			}
			
			return item;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Cannot remove elements from Wig file");
		}
	}
}
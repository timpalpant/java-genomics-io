package edu.unc.genomics.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.broad.igv.bbfile.WigItem;

public class VariableStepContig extends Contig {
	
	private static final Logger log = Logger.getLogger(VariableStepContig.class);
	
	private static final long serialVersionUID = 3139905829545756903L;

	public VariableStepContig(String chr, int start, int stop, int span) {
		super(chr, start, stop, span);
	}
	
	public static VariableStepContig parse(String headerLine) throws WigFileException {
		String[] tokens = headerLine.split(" ");
		if (tokens.length == 0 || !tokens[0].equals(Contig.VARIABLE_STEP)) {
			throw new WigFileException("Not a valid variableStep header line: " + headerLine);
		}
		
		String chr = "";
		int start = 1;
		int span = 1;
		for (int i = 1; i < tokens.length; i++ ) {
			String s = tokens[i];
			String[] pair = s.split("=");
			if (pair.length != 2) {
				throw new WigFileException("Invalid keypair in variableStep header line: " + s);
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
				throw new WigFileException("Invalid attribute in variableStep header line: " + key);
			}
		}
		
		return new VariableStepContig(chr, start, -1, span);
	}
	
	@Override
	public String toString() {
		return Contig.VARIABLE_STEP + " chrom=" + getChr() + " span=" + getSpan();
	}

	@Override
	public VariableStepContigIterator query(RandomAccessFile raf, String chr, 
			int start, int stop) throws IOException, WigFileException {
		
		return new VariableStepContigIterator(raf, chr, start, stop);
	}
	
	private class VariableStepContigIterator implements Iterator<WigItem> {

		private final RandomAccessFile raf;
		private final String chr;
		private final int low;
		private final int high;

		private int itemIndex = 0;
		private int bp;
		private float value;
		private boolean hasNextLine = false;
		
		public VariableStepContigIterator(final RandomAccessFile raf, final String chr, 
				final int start, final int stop) throws IOException, WigFileException {
			this.raf = raf;
			this.chr = chr;
			
			// Clamp to bases that are covered by this Contig
			low = Math.max(getStart(), start);
			high = Math.min(getStop(), stop);
			
			// Find the closest known upstream base-pair position
			int closestUpstream = getUpstreamIndexedBP(low);
			raf.seek(getIndex(closestUpstream));
			
			// Move to the first item
			String line;
			while ((line = raf.readLine()) != null) {
				// Break if at the next Contig
				if (line.startsWith(Contig.FIXED_STEP) || line.startsWith(Contig.VARIABLE_STEP)) {
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
		public WigItem next() {
			WigItem item = new WigItem(itemIndex++, chr, bp, bp+getSpan()-1, value);
			
			try {
				String line = raf.readLine();
				if (line == null || line.startsWith(Contig.FIXED_STEP) || line.startsWith(Contig.VARIABLE_STEP)) {
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
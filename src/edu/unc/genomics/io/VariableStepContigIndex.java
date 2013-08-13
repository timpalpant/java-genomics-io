package edu.unc.genomics.io;

import java.io.IOException;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import ed.javatools.BufferedRandomAccessFile;
import edu.unc.genomics.Contig;
import edu.unc.genomics.Interval;

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
	public boolean isFixedStep() {
		return false;
	}

	@Override
	public boolean isVariableStep() {
		return true;
	}

	@Override
	public void fill(BufferedRandomAccessFile raf, Interval interval, float[] values) throws WigFileException, IOException {
		// Clamp to bases that are covered by this Contig
		int low = Math.max(getStart(), interval.low());
		int high = Math.min(getStop(), interval.high());
		
		// Find the closest known upstream base-pair position
		int closestUpstream = getUpstreamIndexedBP(low);
		synchronized (raf) {
			// Seek to the closest known position in the index
			raf.seek(getIndex(closestUpstream));
			
			// Load the data from the file into the values array
			String line;
			int bp = low;
			while ((line = raf.readLine2()) != null && bp <= high) {
				// Break if at the next Contig
				if (line.startsWith("track") || 
            line.startsWith(Contig.Type.FIXEDSTEP.getId()) || 
            line.startsWith(Contig.Type.VARIABLESTEP.getId())) {
					break;
				}
				
				int delim = line.indexOf('\t');
				bp = Integer.parseInt(line.substring(0, delim));
				if (bp + getSpan() - 1 >= low) {
					float value = Float.parseFloat(line.substring(delim+1));
					if (!Float.isNaN(value)) {
						for (int i = bp; i <= bp+getSpan()-1; i++) {
							if (interval.includes(i)) {
								values[i-interval.low()] = value;
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void fillStats(BufferedRandomAccessFile raf, Interval interval, SummaryStatistics stats) throws WigFileException, IOException {
		// Clamp to bases that are covered by this Contig
		int low = Math.max(getStart(), interval.low());
		int high = Math.min(getStop(), interval.high());
		
		// Find the closest known upstream base-pair position
		int closestUpstream = getUpstreamIndexedBP(low);
		synchronized (raf) {
			// Seek to the closest known position in the index
			raf.seek(getIndex(closestUpstream));
			
			// Load the data from the file into the values array
			String line;
			int bp = low;
			while ((line = raf.readLine2()) != null && bp <= high) {
				// Break if at the next Contig
				if (line.startsWith("track") || 
            line.startsWith(Contig.Type.FIXEDSTEP.getId()) || 
            line.startsWith(Contig.Type.VARIABLESTEP.getId())) {
					break;
				}
				
				int delim = line.indexOf('\t');
				bp = Integer.parseInt(line.substring(0, delim));
				if (bp + getSpan() - 1 >= low) {
					float value = Float.parseFloat(line.substring(delim+1));
					if (!Float.isNaN(value)) {
						for (int i = bp; i <= bp+getSpan()-1; i++) {
							if (interval.includes(i)) {
								stats.addValue(value);
							}
						}
					}
				}
			}
		}
	}
}
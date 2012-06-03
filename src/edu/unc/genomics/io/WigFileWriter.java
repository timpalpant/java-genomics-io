package edu.unc.genomics.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.apache.log4j.Logger;

import edu.ucsc.genome.TrackHeader;
import edu.unc.genomics.Contig;

/**
 * A class for writing data to Wiggle files in either fixedStep or variableStep format
 * 
 * @author timpalpant
 *
 */
public class WigFileWriter implements Closeable {
	
	private static final Logger log = Logger.getLogger(WigFileWriter.class);
	
	// The format for writing numerical (float) values into Wig files
	private static final DecimalFormat formatter = new DecimalFormat();
	static {
		formatter.setMaximumFractionDigits(8);
		formatter.setGroupingUsed(false);
		DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
		symbols.setInfinity("Inf");
		symbols.setNaN("NaN");
		formatter.setDecimalFormatSymbols(symbols);
	}
	
	private final Path p;
	private final PrintWriter writer;
	
	/**
	 * Write or append to a Wiggle file
	 * @param p the Path to the Wig file
	 * @param options if the file should be opened for appending, etc.
	 * @throws IOException if a disk write error occurs
	 */
	public WigFileWriter(Path p, OpenOption... options) throws IOException {
		this.p = p;
		log.debug("Initializing Wig file writer "+p);
		this.writer = new PrintWriter(Files.newBufferedWriter(p, Charset.defaultCharset(), options));
	}
	
	/**
	 * Construct a new Wiggle file with the given header
	 * @param p the Path to the Wig file
	 * @param header the header for the new Wig file
	 * @throws IOException if a disk write error occurs
	 */
	public WigFileWriter(Path p, TrackHeader header) throws IOException {
		this(p);
		
		// Write the header to the output file
		if (header.getType() != TrackHeader.Type.WIGGLE) {
			log.error("Refusing to write track header with type="+header.getType().getId()+" to Wig file");
		} else {
			log.debug("Writing Wig file header: "+header);
			writer.println(header);
		}
	}

	@Override
	public final void close() throws IOException {
		log.debug("Closing Wig file writer "+p);
		writer.close();
	}
	
	/**
	 * Add a new Contig of values to this Wig file
	 * The most compact format will automatically be chosen (fixedStep/variableStep) based on sparsity and layout
	 * and it will be written with the largest resolution that still resolves all features in the data
	 * @param contig the Contig of values to write to this Wig file
	 */
	public final void write(final Contig contig) {
		if(contig.isFixedStep()) {
			writeFixedStepContig(contig);
		} else {
			float sparsity = ((float) contig.coverage()) / contig.length();
			if (sparsity < 0.55 || contig.getVariableStepSpan() > 1) {
				writeVariableStepContig(contig);
			} else {
				writeFixedStepContig(contig);
			}
		}
	}
	
	/**
	 * Add a new Contig of values to this Wig file in fixedStep format
	 * Resolution (step/span) will be automatically chosen so that the output is
	 * as compact as possible while still resolving all features in the data.
	 * @param contig the Contig of values to write to this Wig file
	 * @return the Future corresponding to this Contig's write job
	 */
	public final void writeFixedStepContig(final Contig contig) {
		log.debug("Writing contig: "+contig.getFixedStepHeader());
		synchronized (writer) {
			writer.println(contig.getFixedStepHeader());
			int step = contig.getMinStep();
			for (int bp = contig.getFirstBaseWithData(); bp <= contig.high(); bp += step) {
				writer.println(formatter.format(contig.get(bp)));
			}
		}
	}
	
	/**
	 * Add a new Contig of values to this Wig file in variableStep format
	 * Resolution (span) will be automatically chosen so that the output is
	 * as compact as possible while still resolving all features in the data.
	 * @param contig the Contig of values to write to this Wig file
	 * @return 
	 */
	public final void writeVariableStepContig(final Contig contig) {
		log.debug("Writing contig: "+contig.getVariableStepHeader());
		synchronized (writer) {
			writer.println(contig.getVariableStepHeader());
			int bp = contig.getFirstBaseWithData();
			int span = contig.getVariableStepSpan();
			while (bp <= contig.high()) {
				float value = contig.get(bp);
				// Write the value and skip the span size
				if (!Float.isNaN(value)) {
					writer.println(bp+"\t"+formatter.format(value));
					bp += span;
				} else {
					bp++;
				}
			}
		}
	}

	/**
	 * @return the path
	 */
	public final Path getPath() {
		return p;
	}

}

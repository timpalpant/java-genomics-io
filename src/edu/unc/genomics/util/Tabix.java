package edu.unc.genomics.util;

import java.nio.file.Path;

import org.apache.log4j.Logger;

import net.sf.samtools.TabixWriter;

/**
 * Utility methods for working with Tabix files
 * @author timpalpant
 *
 */
public class Tabix {
	
	private static final Logger log = Logger.getLogger(Tabix.class);
	
	public static void bgzip(Path input, Path output) {

	}
	
	public static void index(Path file, TabixWriter.Conf conf) {
		
	}
	
}

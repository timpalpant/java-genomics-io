package edu.unc.genomics.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.log4j.Logger;

import net.sf.samtools.TabixWriter;
import net.sf.samtools.TabixWriter.TabixException;
import net.sf.samtools.util.BlockCompressedOutputStream;

/**
 * Utility methods for working with Tabix files
 * @author timpalpant
 *
 */
public class Tabix {
	
	private static final Logger log = Logger.getLogger(Tabix.class);
	
	/**
	 * BGZip an interval file for use with Tabix
	 * @param input the input interval file in ASCII text format
	 * @param output the bgzipped output file
	 * @throws IOException
	 */
	public static void bgzip(Path input, Path output) throws IOException {
		log.debug("BGZipping "+input+" for Tabix indexing");
		InputStream is = Files.newInputStream(input);
		BufferedInputStream bis = new BufferedInputStream(is);
		
		BlockCompressedOutputStream bcos = new BlockCompressedOutputStream(output.toFile());
		int data = -1;
		while ((data = bis.read()) != -1) {
			bcos.write(data);
		}
		
		bcos.close();
		bis.close();
		is.close();
	}
	
	/** 
	 * Index a file with Tabix for random lookups
	 * @param file the bgzipped, sorted file to index
	 * @param conf the configuration to use (specifying the chr, start, stop cols)
	 * @return the path to the Tabix index file
	 * @throws IOException
	 * @throws TabixException
	 */
	public static Path index(Path file, TabixWriter.Conf conf) throws IOException, TabixException {
		log.debug("Indexing "+file+" with Tabix");
		TabixWriter writer = new TabixWriter(file, conf);
		return writer.createIndex();
	}
	
}

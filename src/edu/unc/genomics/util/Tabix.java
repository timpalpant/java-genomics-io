package edu.unc.genomics.util;

import it.unipi.di.util.ExternalSort;

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
	
	public static void sortFile(Path input, Path output, TabixWriter.Conf conf) throws IOException {
		log.debug("Sorting "+input+" for Tabix indexing");
		ExternalSort sorter = new ExternalSort();
		sorter.setInFile(input.toString());
		sorter.setOutFile(output.toString());
		sorter.setNumeric(true);
		
		int[] columns = new int[3];
		columns[0] = conf.chrColumn-1;
		columns[1] = conf.startColumn-1;
		columns[2] = conf.endColumn-1;
		sorter.setColumns(columns);
		
		sorter.run();
	}
	
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
	
	public static Path index(Path file, TabixWriter.Conf conf) throws IOException, TabixException {
		log.debug("Indexing "+file+" with Tabix");
		TabixWriter writer = new TabixWriter(file, conf);
		return writer.createIndex();
	}
	
}

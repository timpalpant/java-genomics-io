package edu.unc.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author timpalpant
 * Miscellaneous low-key file utilities
 */
public class FileUtils {
	public static double THRESHOLD = 0.3;

	public static boolean isAsciiText(Path p) throws IOException {
		return isAsciiText(p, THRESHOLD);
	}
	
	public static boolean isAsciiText(Path p, double threshold) throws IOException {
		InputStream fis = Files.newInputStream(p);
		
		int totalCount = 0;
		int binaryCount = 0;
		for (int i = 1; i < 1024; i++) {
			if (fis.available() == 0) { break; }
			int current = fis.read();
			if (current < 32 || current > 127) { binaryCount++; }
			totalCount++;
		}
		
		return ((double)binaryCount)/totalCount < threshold;
	}
}

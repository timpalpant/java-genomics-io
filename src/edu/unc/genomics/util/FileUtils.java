package edu.unc.genomics.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
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
		int totalCount = 0;
		int binaryCount = 0;
		try (InputStream fis = Files.newInputStream(p)) {
			for (int i = 1; i < 1024; i++) {
				if (fis.available() == 0) { break; }
				int current = fis.read();
				if (current < 32 || current > 127) { binaryCount++; }
				totalCount++;
			}
		}
		
		return ((double)binaryCount)/totalCount < threshold;
	}
	
	public static int countLines(Path p) throws IOException {
		int count = 0;
		try (BufferedReader reader = Files.newBufferedReader(p, Charset.defaultCharset())) {
			String line;
			while ((line = reader.readLine()) != null) {
				count++;
			}
		}
		
		return count;
	}
}

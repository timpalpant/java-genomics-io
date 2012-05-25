package edu.unc.genomics.util;

/**
 * Helper utilities for calculating checksums of files
 * including Adler32 and CRC32
 * 
 * @author timpalpant
 *
 */
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

public class ChecksumUtils {
	public static long adler32(Path p) throws IOException {
		return ChecksumUtils.file(p, new Adler32());
	}
	
	public static long crc32(Path p) throws IOException {
		return ChecksumUtils.file(p, new CRC32());
	}
	
	public static long file(Path p, Checksum c) throws IOException {
		try (CheckedInputStream cis = new CheckedInputStream(Files.newInputStream(p), c);
				BufferedInputStream bis = new BufferedInputStream(cis)) {
			while (bis.read() != -1) { }
			return cis.getChecksum().getValue();
		}
	}
}
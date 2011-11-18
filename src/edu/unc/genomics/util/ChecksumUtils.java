package edu.unc.genomics.util;

/**
 * @author timpalpant
 *
 */
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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
		InputStream is = Files.newInputStream(p);
		CheckedInputStream cis = new CheckedInputStream(is, c);
		BufferedInputStream in = new BufferedInputStream(cis);
		while (in.read() != -1) { }
		return cis.getChecksum().getValue();
	}
}
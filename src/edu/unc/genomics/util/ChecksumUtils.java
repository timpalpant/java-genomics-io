package edu.unc.genomics.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

import org.apache.log4j.Logger;

/**
 * Helper utilities for calculating checksums of files including Adler32 and
 * CRC32
 * 
 * @author timpalpant
 *
 */
public class ChecksumUtils {

  private static final Logger log = Logger.getLogger(ChecksumUtils.class);

  public static long adler32(Path p) throws IOException {
    return ChecksumUtils.file(p, new Adler32());
  }

  public static long crc32(Path p) throws IOException {
    return ChecksumUtils.file(p, new CRC32());
  }

  public static long file(Path p, Checksum c) throws IOException {
    log.debug("Calculating checksum for " + p);
    byte[] buf = new byte[131_136];
    try (CheckedInputStream cis = new CheckedInputStream(Files.newInputStream(p), c)) {
      while (cis.read(buf, 0, buf.length) != -1)
        ;
      log.debug("Checksum = " + cis.getChecksum().getValue());
      return cis.getChecksum().getValue();
    }
  }
}
package edu.unc.genomics.util;

import java.util.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Adapted for java-genomics-io and updated to Java 7 by Timothy Palpant
 * 
 * Goal: offer a generic external-memory sorting program in Java.
 * 
 * It must be : hackable (easy to adapt), scalable to large files, sensibly
 * efficient.
 * 
 * This software is in the public domain.
 * 
 * Usage: java com/google/code/externalsorting/ExternalSort somefile.txt out.txt
 * 
 * You can change the default maximal number of temporary files with the -t
 * flag: java com/google/code/externalsorting/ExternalSort somefile.txt out.txt
 * -t 3
 * 
 * For very large files, you might want to use an appropriate flag to allocate
 * more memory to the Java VM: java -Xms2G
 * com/google/code/externalsorting/ExternalSort somefile.txt out.txt
 * 
 * By (in alphabetical order) Philippe Beaudoin, Jon Elsas, Christan Grant,
 * Daniel Haran, Daniel Lemire, April 2010 originally posted at
 * http://www.daniel
 * -lemire.com/blog/archives/2010/04/01/external-memory-sorting-in-java/
 */
public class ExternalSort {

  public static int DEFAULTMAXTEMPFILES = 1024;

  // we divide the file into small blocks. If the blocks
  // are too small, we shall create too many temporary files.
  // If they are too big, we shall be using too much memory.
  public static long estimateBestSizeOfBlocks(Path fileToBeSorted, int maxTmpFiles) throws IOException {
    /**
     * We multiply by two because later on someone insisted on counting the
     * memory usage as 2 bytes per character. By this model, loading a file with
     * 1 character will use 2 bytes.
     */
    long sizeoffile = 2 * Files.size(fileToBeSorted);

    // we don't want to open up much more than maxtmpfiles temporary files,
    // better run out of memory first.
    long blocksize = sizeoffile / maxTmpFiles + (sizeoffile % maxTmpFiles == 0 ? 0 : 1);

    // on the other hand, we don't want to create many temporary files
    // for naught. If blocksize is smaller than half the free memory, grow it.
    long freemem = Runtime.getRuntime().freeMemory();
    if (blocksize < freemem / 2) {
      blocksize = freemem / 2;
    }

    return blocksize;
  }

  /**
   * This will simply load the file by blocks of x rows, then sort them
   * in-memory, and write the result to temporary files that have to be merged
   * later.
   * 
   * @param file
   *          some flat file
   * @param cmp
   *          string comparator
   * @return a list of temporary flat files
   */
  public static List<Path> sortInBatch(Path p, Comparator<String> cmp) throws IOException {
    return sortInBatch(p, cmp, DEFAULTMAXTEMPFILES, Charset.defaultCharset());
  }

  /**
   * This will simply load the file by blocks of x rows, then sort them
   * in-memory, and write the result to temporary files that have to be merged
   * later. You can specify a bound on the number of temporary files that will
   * be created.
   * 
   * @param file
   *          some flat file
   * @param cmp
   *          string comparator
   * @param maxtmpfiles
   *          maximal number of temporary files
   * @param Charset
   *          character set to use
   * @return a list of temporary flat files
   */
  public static List<Path> sortInBatch(Path p, Comparator<String> cmp, int maxtmpfiles, Charset cs) throws IOException {
    List<Path> files = new ArrayList<Path>();
    BufferedReader fbr = Files.newBufferedReader(p, cs);
    long blocksize = estimateBestSizeOfBlocks(p, maxtmpfiles); // in bytes

    try {
      List<String> tmplist = new ArrayList<String>();
      String line = "";
      try {
        while (line != null) {
          long currentblocksize = 0;// in bytes
          while ((currentblocksize < blocksize) && ((line = fbr.readLine()) != null)) { // as
                                                                                        // long
                                                                                        // as
                                                                                        // you
                                                                                        // have
                                                                                        // enough
                                                                                        // memory
            tmplist.add(line);
            currentblocksize += line.length() * 2; // java uses 16 bits per
                                                   // character?
          }
          files.add(sortAndSave(tmplist, cmp, cs));
          tmplist.clear();
        }
      } catch (EOFException oef) {
        if (tmplist.size() > 0) {
          files.add(sortAndSave(tmplist, cmp, cs));
          tmplist.clear();
        }
      }
    } finally {
      fbr.close();
    }

    return files;
  }

  public static Path sortAndSave(List<String> tmplist, Comparator<String> cmp, Charset cs) throws IOException {
    Collections.sort(tmplist, cmp);
    Path tmpFile = Files.createTempFile("sortInBatch", "flatfile");
    tmpFile.toFile().deleteOnExit();

    try (BufferedWriter fbw = Files.newBufferedWriter(tmpFile, cs)) {
      for (String r : tmplist) {
        fbw.write(r);
        fbw.newLine();
      }
    }

    return tmpFile;
  }

  /**
   * This merges a bunch of temporary flat files
   * 
   * @param files
   * @param output
   *          file
   * @return The number of lines sorted. (P. Beaudoin)
   */
  public static int mergeSortedFiles(List<Path> files, Path outputFile, final Comparator<String> cmp)
      throws IOException {
    return mergeSortedFiles(files, outputFile, cmp, Charset.defaultCharset());
  }

  /**
   * This merges a bunch of temporary flat files
   * 
   * @param files
   * @param output
   *          file
   * @param Charset
   *          character set to use to load the strings
   * @return The number of lines sorted. (P. Beaudoin)
   */
  public static int mergeSortedFiles(List<Path> files, Path outputFile, final Comparator<String> cmp, Charset cs)
      throws IOException {
    PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<BinaryFileBuffer>(11, new Comparator<BinaryFileBuffer>() {
      public int compare(BinaryFileBuffer i, BinaryFileBuffer j) {
        return cmp.compare(i.peek(), j.peek());
      }
    });

    for (Path p : files) {
      BinaryFileBuffer bfb = new BinaryFileBuffer(p, cs);
      pq.add(bfb);
    }

    int rowCounter = 0;
    try (BufferedWriter fbw = Files.newBufferedWriter(outputFile, cs)) {
      while (pq.size() > 0) {
        BinaryFileBuffer bfb = pq.poll();
        String r = bfb.pop();
        fbw.write(r);
        fbw.newLine();
        ++rowCounter;
        if (bfb.empty()) {
          bfb.close();
          Files.deleteIfExists(bfb.p); // we don't need you anymore
        } else {
          pq.add(bfb); // add it back
        }
      }
    } finally {
      for (BinaryFileBuffer bfb : pq) {
        bfb.close();
      }
    }

    return rowCounter;
  }

}

class BinaryFileBuffer {

  public BufferedReader fbr;
  public Path p;
  private String cache;
  private boolean empty;

  public BinaryFileBuffer(Path p, Charset cs) throws IOException {
    this.p = p;
    fbr = Files.newBufferedReader(p, cs);
    reload();
  }

  public boolean empty() {
    return empty;
  }

  private void reload() throws IOException {
    try {
      if ((this.cache = fbr.readLine()) == null) {
        empty = true;
        cache = null;
      } else {
        empty = false;
      }
    } catch (EOFException e) {
      empty = true;
      cache = null;
    }
  }

  public void close() throws IOException {
    fbr.close();
  }

  public String peek() {
    if (empty()) {
      return null;
    }

    return cache.toString();
  }

  public String pop() throws IOException {
    String answer = peek();
    reload();
    return answer;
  }

}
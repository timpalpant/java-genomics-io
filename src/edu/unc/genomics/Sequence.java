package edu.unc.genomics;

/**
 * Defines behavior for an object that represents a genomic sequence
 * 
 * @author timpalpant
 *
 */
public interface Sequence {
  /**
   * Get the DNA sequence associated with this Sequence
   * 
   * @return the DNA sequence (A,T,C,G,N)
   */
  String getSequence();

  /**
   * Get the quality score encoding associated with this Sequence, or null if
   * there is no available quality information. The quality score may be in any
   * ASCII encoding format (Sanger, Illumina, etc.)
   * 
   * @return the quality score for this sequence in ASCII-encoding
   */
  String getQualities();
}

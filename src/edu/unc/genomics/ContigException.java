package edu.unc.genomics;

/**
 * Exception thrown when attempting to set a value in a Contig that is outside
 * the range
 * 
 * @author timpalpant
 *
 */
public class ContigException extends RuntimeException {

  private static final long serialVersionUID = 5101389182563499491L;

  public ContigException() {
    // TODO Auto-generated constructor stub
  }

  public ContigException(String message) {
    super(message);
    // TODO Auto-generated constructor stub
  }

  public ContigException(Throwable cause) {
    super(cause);
    // TODO Auto-generated constructor stub
  }

  public ContigException(String message, Throwable cause) {
    super(message, cause);
    // TODO Auto-generated constructor stub
  }

  public ContigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    // TODO Auto-generated constructor stub
  }

}

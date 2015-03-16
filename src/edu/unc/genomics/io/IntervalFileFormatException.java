package edu.unc.genomics.io;

/**
 * Thrown when a (likely) unrecoverable format error is found in an Interval
 * file
 * 
 * @author timpalpant
 *
 */
public class IntervalFileFormatException extends RuntimeException {

  private static final long serialVersionUID = 7459693587746345198L;

  /**
	 * 
	 */
  public IntervalFileFormatException() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   */
  public IntervalFileFormatException(String message) {
    super(message);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param cause
   */
  public IntervalFileFormatException(Throwable cause) {
    super(cause);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   * @param cause
   */
  public IntervalFileFormatException(String message, Throwable cause) {
    super(message, cause);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public IntervalFileFormatException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    // TODO Auto-generated constructor stub
  }

}

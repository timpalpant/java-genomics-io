package edu.ucsc.genome;

/**
 * @author timpalpant
 * Exception thrown when a parsing error occurs in a UCSC TrackHeader
 */
public class TrackHeaderException extends Exception {

	/**
	 * 
	 */
	public TrackHeaderException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public TrackHeaderException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public TrackHeaderException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public TrackHeaderException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public TrackHeaderException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}

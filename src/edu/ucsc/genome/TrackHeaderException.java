package edu.ucsc.genome;

/**
 * Exception thrown when a parsing error occurs in a UCSC TrackHeader
 * 
 * @author timpalpant
 * 
 */
public class TrackHeaderException extends Exception {

	private static final long serialVersionUID = -515886535133569480L;

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

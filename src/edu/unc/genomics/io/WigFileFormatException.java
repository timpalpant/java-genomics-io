package edu.unc.genomics.io;

/**
 * Exception thrown when there is a (likely) unrecoverable formatting error in a text Wig file
 * @author timpalpant
 *
 */
public class WigFileFormatException extends RuntimeException {

	private static final long serialVersionUID = 5605887792955081973L;

	public WigFileFormatException() {
		// TODO Auto-generated constructor stub
	}

	public WigFileFormatException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public WigFileFormatException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public WigFileFormatException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public WigFileFormatException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}

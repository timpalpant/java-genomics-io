package edu.unc.genomics.io;

import java.util.zip.DataFormatException;

/**
 * Exception thrown when trying to query a Wig file for an invalid interval
 * or if there is an error parsing an invalid Wig file
 * 
 * @author timpalpant
 *
 */
public class WigFileException extends DataFormatException {

	private static final long serialVersionUID = 3824318160375643583L;

	/**
	 * 
	 */
	public WigFileException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public WigFileException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}

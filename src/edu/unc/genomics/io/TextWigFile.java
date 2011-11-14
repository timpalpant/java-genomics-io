package edu.unc.genomics.io;

import java.nio.file.Path;

import edu.unc.genomics.Contig;

/**
 * @author timpalpant
 *
 */
public class TextWigFile extends WigFile {

	/**
	 * @param p
	 */
	public TextWigFile(Path p) {
		super(p);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see edu.unc.genomics.io.WigFile#query(java.lang.String, int, int)
	 */
	@Override
	public Contig query(String chr, int start, int stop) {
		// TODO Auto-generated method stub
		return null;
	}

}

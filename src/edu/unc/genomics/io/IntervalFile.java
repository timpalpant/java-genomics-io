package edu.unc.genomics.io;

import java.nio.file.Path;

/**
 * @author timpalpant
 *
 */
public abstract class IntervalFile {
	protected Path p;
	
	protected IntervalFile(Path p) {
		this.p = p;
	}
	
	public static IntervalFile autodetect(Path p) {
		
	}
}

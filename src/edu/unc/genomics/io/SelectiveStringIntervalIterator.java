package edu.unc.genomics.io;

import java.util.Iterator;

import edu.unc.genomics.Interval;
import edu.unc.genomics.IntervalFactory;

/**
 * @author timpalpant
 *
 */
public class SelectiveStringIntervalIterator<T extends Interval> extends StringIntervalIterator<T> {

	private T nextInterval;
	private final String chr;
	private final int start;
	private final int stop;
	
	public SelectiveStringIntervalIterator(Iterator<String> it, 
			IntervalFactory<T> factory, String chr, int start, int stop) {
		super(it, factory);
		this.chr = chr;
		this.start = start;
		this.stop = stop;
		advance();
	}

	@Override
	public boolean hasNext() {
		return (nextInterval != null);
	}

	@Override
	public T next() {
		T temp = nextInterval;
		advance();
		return temp;
	}
	
	private void advance() {
		nextInterval = null;
		while (nextInterval == null && it.hasNext()) {
			String line = it.next();
			T interval = factory.parse(line);
			
			// Validate that the interval meets the selection criteria (overlaps the specified window)
			if (interval != null && interval.getChr().equals(chr) && interval.getStop() >= start && interval.getStart() <= stop) {
				nextInterval = interval;
			}
		}
	}
}

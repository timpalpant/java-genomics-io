package edu.unc.genomics.io;

import java.util.Iterator;

import edu.unc.genomics.Interval;
import edu.unc.genomics.IntervalFactory;

/**
 * @author timpalpant
 * Iterator that iterates over lines in a text file and returns each line
 * as a parsed interval using an IntervalFactory
 */
class StringIntervalIterator<T extends Interval> implements Iterator<T> {

	protected Iterator<String> it;
	protected IntervalFactory<T> factory;
	
	public StringIntervalIterator(Iterator<String> it, IntervalFactory<T> factory) {
		this.it = it;
		this.factory = factory;
	}
	
	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public T next() {
		String line = it.next();
		return factory.parse(line);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot remove lines from TextIntervalFile");
	}
}
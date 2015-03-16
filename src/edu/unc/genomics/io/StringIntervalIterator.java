package edu.unc.genomics.io;

import java.util.Iterator;

import edu.unc.genomics.Interval;
import edu.unc.genomics.IntervalFactory;

/**
 * Iterator that iterates over lines in a text file and returns each line as a
 * parsed interval using an IntervalFactory
 * 
 * @author timpalpant
 * 
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
    T interval = null;
    while ((interval == null) && it.hasNext()) {
      String line = it.next();
      interval = factory.parse(line);
    }

    return interval;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Cannot remove lines from TextIntervalFile");
  }
}
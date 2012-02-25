package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;

import net.sf.samtools.TabixReader;

import edu.unc.genomics.Interval;
import edu.unc.genomics.IntervalFactory;

/**
 * @author timpalpant
 *
 */
public class TabixFile<T extends Interval> extends IntervalFile<T> {

	private TabixReader reader;
	private IntervalFactory<T> factory;
	
	protected TabixFile(Path p, IntervalFactory<T> factory) throws IOException {
		super(p);
		this.factory = factory;
		reader = new TabixReader(p);
	}

	@Override
	public Iterator<T> iterator() {
		return new StringIntervalIterator<T>(reader.iterator(), factory);
	}

	@Override
	public void close() throws IOException { }

	@Override
	public Iterator<T> query(String chr, int start, int stop) {
		return new StringIntervalIterator<T>(reader.query(chr, start, stop), factory);
	}

	@Override
	public int count() {
		// FIXME: Efficiently count Tabix entries
		int count = 0;
		for (Interval i : this) {
			count++;
		}
		return count;
	}

	@Override
	public Set<String> chromosomes() {
		return reader.chromosomes();
	}

}

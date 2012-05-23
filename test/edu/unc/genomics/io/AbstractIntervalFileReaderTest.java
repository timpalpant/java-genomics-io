package edu.unc.genomics.io;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import edu.unc.genomics.Interval;

public abstract class AbstractIntervalFileReaderTest {
	
	protected IntervalFileReader<? extends Interval> test;

	@After
	public void tearDown() throws Exception {
		if (test != null) {
			test.close();
		}
	}

	@Test
	public void testAutodetect() throws IntervalFileSnifferException, IOException {
		IntervalFileReader<? extends Interval> auto = IntervalFileReader.autodetect(test.getPath());
	}

	@Test
	public void testLoadAllPath() throws IntervalFileSnifferException, IOException {
		List<? extends Interval> list = IntervalFileReader.loadAll(test.getPath());
	}

	@Test
	public void testLoadAll() {
		List<? extends Interval> list = test.loadAll();
	}

}

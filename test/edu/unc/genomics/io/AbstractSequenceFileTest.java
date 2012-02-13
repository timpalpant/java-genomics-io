package edu.unc.genomics.io;

import java.io.IOException;

import org.junit.After;
import org.junit.Test;

import edu.unc.genomics.Sequence;

public class AbstractSequenceFileTest {
	
	protected SequenceFile<? extends Sequence> test;

	@After
	public void tearDown() throws Exception {
		if (test != null) {
			test.close();
		}
	}

	@Test
	public void testAutodetect() throws SequenceFileSnifferException, IOException {
		SequenceFile.autodetect(test.getPath());
	}

	@Test
	public void testLoadAllPath() throws SequenceFileSnifferException, IOException {
		SequenceFile.loadAll(test.getPath());
	}

	@Test
	public void testLoadAll() {
		test.loadAll();
	}

}

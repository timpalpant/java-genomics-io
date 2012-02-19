/*
 * Copyright © 2006 - 2007 Claudio Corsi, Paolo Ferragina
 *
 * This file is part of SmallText library.
 *
 * SmallText is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SmallText is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.unipi.di.util;

import it.unimi.dsi.mg4j.io.FastBufferedReader;
import it.unimi.dsi.mg4j.util.MutableString;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * A multi-way external merge sort that use a TreeMap (Java Red-Black Tree) as 
 * internal sorting data structure. This implementation is 100% pure Java and 
 * is able to scale over GBs of data better than the unix <code>sort</code> command.
 * 
 * Adapted for java-genomics-io by Timothy Palpant.
 * 
 * @author Claudio Corsi
 * @author Paolo Ferragina
 * @author Timothy Palpant
 * 
 */
public class ExternalSort {
	
	public static final long DEFAULT_RUN_SIZE = (long)(50 * Math.pow(2, 20)); 	// 50MB
	public static final int DEFAULT_PAGE_SIZE = 102400; 	// 100KB
	
	public static final String VERSION = "0.1.3 - run!";
		
	protected HashMap<Long, Reader> runsMap;	// # run -> file reader
	protected InputStream in = System.in;
	protected PrintStream out = System.out;
	protected String outfile;
	protected String infile;
	protected int[] columns = new int[0];
	protected char sep = '\t';
	protected long runSize = DEFAULT_RUN_SIZE;
	protected int pageSize = DEFAULT_PAGE_SIZE;
	protected long elapsedSecs = 0;
	
	protected long numberOfDumpedRows = 0;
	protected long numberOfInputRows = 0;
	
	protected boolean reverse = false;
	protected boolean numeric = false;
	protected boolean uniq = false;
	protected String currKey = null;
	protected boolean dist = false;
	protected String prevKey = null;
	protected long rowsCount = 0;
	
	protected boolean EOF = false;   // End of file while reading the current run
	protected MutableString buff = new MutableString(1024);  	// 1KB buffer size
	
	protected boolean extract = false;
	
	private TreeMap<String, Tuple> map;
	
	
	/** 
	 * Create a new ExternalSort.
	 * 
	 */
	public ExternalSort() {}
	
	/**
	 * A reverse comparator.
	 * 
	 * @author data
	 *
	 */
	public class ReverseComparator implements Comparator<String> {
		public final int compare(String o1, String o2) {
			return -o1.compareTo(o2);
		}
	}
	
	/**
	 * Used to store all the Strings associated to a sorting key with their run's ids.
	 * 
	 * @author Claudio Corsi
	 *
	 */
	protected class Tuple {

		public List<String> lines = new ArrayList<String>();
		public long[] run;
		
		public Tuple() {}
		
		public final void appendRun(long runNumber) {
			
			if (run == null) {
				run = new long[] { runNumber };
				return;
			}
			
			int size = run.length + 1;
			long[] tmp = new long[size];
			System.arraycopy(run, 0, tmp, 0, run.length);
			tmp[size - 1] = runNumber;
			run = tmp;
		}
		
		public void append(String x) {
			lines.add(x);
		}
	}
	
	/**
	 * Used to compare two strings by their sorting columns (aka sorted keys).
	 * 
	 * @author Claudio Corsi
	 *
	 */
	protected class SortingKey implements Comparable<SortingKey> {
		
		public String key, row;
		
		private boolean reverse = false;
		
		public SortingKey(String row, String key) {
			this.row = row;
			this.key = key;
		}
		
		public SortingKey(String row, String cols, boolean reverse) {
			this.row = row;
			this.key = cols;
			this.reverse = reverse;
		}
		
		public void setReverse(boolean reverse) {
			this.reverse = reverse;
		}
		
		public int length() { 
			return (key != row) ? key.length() + row.length() : row.length();
		}
		
		public final int compareTo(SortingKey k) {
			return (reverse) ? -key.compareTo(k.key) : key.compareTo(k.key);
		}
	}
	
	/**
	 * Set true to sort in reverse order (ascendent instead of descendent).
	 * 
	 * @param reverse
	 */
	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}
	
	/**
	 * Compare the sorting values (rows or columns) as numerical values. In other
	 * words this flag will cause the right padding of the sorting values. 
	 * 
	 * @param numeric
	 */
	public void setNumeric(boolean numeric) {
		this.numeric = numeric;
	}
	
	/**
	 * Instead to sort, dump the frequencies of the
	 * sorting keys in their sorted order (not in the frequency values order). 
	 * 
	 * @param dist
	 */
	public void setKeysDistribution(boolean dist) {
		this.dist = dist;
	}
	
	/**
	 * Remove duplicates from the result. In case of equals sorting values (rows or columns) only
	 * one of these is kept. 
	 * 
	 * @param uniq
	 */
	public void setUniq(boolean uniq) {
		this.uniq = uniq;
	}
	
	/**
	 * Set the size of the chunk (run) of text to sort in memory at the first stage of 
	 * the algorithm. This is the maximum size of memory available to sort. If
	 * not set the default value is 50MB.
	 * 
	 * @param runSize the size of memory available expressed in bytes
	 */
	public void setRunSize(long runSize) {
		this.runSize = runSize;
	}

	/**
	 * Set the page size to use in the second stage of the algorithm (pagination of the sorted runs).
	 * This value should be chosen depending on the disk page size. The default value is 
	 * 100KB.
	 * 
	 * @param pageSize the page size expressed in bytes.
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	/**
	 * Set the columns to sort. If not specified then the input file will be sorted considering the entire content
	 * of the rows (divided by new lines). If specified each row will be divided into columns and sorted
	 * accordingly to the value of the specified rows. The order of the columns matter. For example
	 * if the columns list is [1, 0, 5] then for first the column 1 of each row is compared. In case
	 * of the same value the column 0 is compared and, at the end, the column 5. 
	 * Two rows are considered equals if all the specified columns contain the same value
	 * 
	 * @see #setUniq(boolean)
	 * @see #setSeparator(char) 
	 * 
	 * @param columns the list of columns to sort
	 */
	public void setColumns(int[] columns) {
		this.columns = columns;
	}

	/**
	 * Set the character to use to split the rows in columns. Default value is tab ('\t').
	 *   
	 * @param sep
	 */
	public void setSeparator(char sep) {
		this.sep = sep;
	}
	
	/**
	 * If true, dump out only the sorting column(s) omitting the other ones.
	 * The dumped column(s) will be sorted respecting the sorting parameters.
	 * If no columns are selected (sort by the entire rows) this option
	 * doesn't have effect. 
	 * 
	 * @param extract true to dump out only the sorting column(s)
	 */
	public void setExtract(boolean extract) {
		this.extract = extract;
	}

	private void resetProgressInfos() {
		// reset some class value
	    elapsedSecs = 0;
	    numberOfDumpedRows = 0;
	    numberOfInputRows = 0;
	}
	
	/**
	 * Set the output file. By default the result is written in stdout.
	 * 
	 * @param outfile
	 * @throws FileNotFoundException
	 */
	public void setOutFile(String outfile) throws FileNotFoundException {
		this.outfile = outfile;
	}
	
	/**
	 * Set the input file to sort. By default is the stdin.  
	 * 
	 * @param infile
	 * @throws FileNotFoundException
	 */
	public void setInFile(String infile) throws FileNotFoundException {
		this.infile = infile;
		this.in = new FileInputStream(infile);
	}
	
	protected void updateProgressInfos(long start) {
		long rowsPerSec = 0;
		elapsedSecs = (System.currentTimeMillis() - start) / 1000;
		if (elapsedSecs > 0) rowsPerSec = numberOfDumpedRows / elapsedSecs;
		System.out.println(" >> Input rows: " + numberOfInputRows + ", dumped rows: " + numberOfDumpedRows + " @ " + rowsPerSec + " rows/sec.");
	}
	
	/**
	 * Start the sorting process. This method can take much time to complete.
	 * 
	 * @throws IOException
	 */
	public void run() throws IOException {
		FastBufferedReader fbr = new FastBufferedReader(new InputStreamReader(in));
				
		runsMap = new HashMap<Long, Reader>();

		List<SortingKey> chunk = new ArrayList<SortingKey>();
		
		long chunkCounter = 0;
		long tmpFileSize = 0;
		rowsCount = 0;
		
		while(!EOF) {
			
			int currChunkSize = 0;
			
			while(currChunkSize <= runSize) {
				
				if (fbr.readLine(buff) == null) {
					EOF = true;
					break;
				}
				
				String line = buff.toString();
				String key = Utils.getKey(line, columns, sep, numeric);
				SortingKey s = new SortingKey(line, key, reverse);
				chunk.add(s);
				
				rowsCount++;
				currChunkSize += buff.length();  // count the number of CHARS
			} 
			
			chunkCounter++;
			
			// a chunk has been loaded, let's sort it...
			File tmpFile = createSortedRun(chunk);
			
			runsMap.put(chunkCounter, new FastBufferedReader(new FileReader(tmpFile)));
			
			tmpFileSize += tmpFile.length();
			chunk.clear();
		}

		fbr.close();
		
		long stop = System.currentTimeMillis();
		
		// Init the in-memory sorting data structure
		initDataStructure();
		
		resetProgressInfos();

		// Loading the first pages
		Iterator<Long> iter = runsMap.keySet().iterator();
		while (iter.hasNext()) {
			Long runNumber = (Long)iter.next();
			loadNextPage(runNumber);
		}

		dumpSortedRows();
		
		stop = System.currentTimeMillis();
		for (Reader r : runsMap.values()) r.close();
	}
	
	protected File createSortedRun(List<SortingKey> chunk) throws IOException {
		
		File tmp = File.createTempFile("run", ".txt");
		tmp.deleteOnExit();
		
		Collections.sort(chunk);

		// FIXME: this is synchronized, can we avoid it?
		BufferedWriter bw = new BufferedWriter(new FileWriter(tmp));
		
		for (SortingKey e : chunk) {
			bw.write(e.row);
			bw.write('\n');
		}
		
		bw.close();
		
		return tmp;
	}
	
	protected void initDataStructure() {
		if (reverse)
			map = new TreeMap<String, Tuple>(new ReverseComparator());
		else
			map = new TreeMap<String, Tuple>();
	}
	
	protected void loadNextPage(long runNumber) throws IOException {
		
		int currPageSize = 0;
		String key = null;
		
		FastBufferedReader reader = (FastBufferedReader)runsMap.get(runNumber);
		
		for (MutableString row = reader.readLine(buff); row != null; row = reader.readLine(buff)) {
			
			String line = buff.toString();
			
			numberOfInputRows++;
			
			currPageSize += buff.length();
			
			key = Utils.getKey(line, columns, sep, numeric);
			
			Tuple tuple = new Tuple();
			Tuple oldTuple = (Tuple)map.put(key, tuple);
			
			if (oldTuple != null) {
				tuple.run = oldTuple.run;
				tuple.lines = oldTuple.lines;
			}
			
			if (!uniq || (tuple.lines.size() == 0 && !key.equals(currKey))) 
				tuple.append(line);
			
			if (currPageSize >= pageSize) {
				tuple.appendRun(runNumber);
				break;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void dumpSortedRows() throws IOException {
		
		if (outfile != null) out = new PrintStream(outfile);
		
		// FIXME: synchronized Writer! Use a not synch one to speed up the I/O 
		BufferedWriter bw = new BufferedWriter(new PrintWriter(out, false), 16384); // 16KB buffer size
		
		long start = System.currentTimeMillis();

		// count how many times a sorting key has been seen
		int freq = 0;
		
		while (!map.isEmpty()) {
			long[] toLoad = null;
			Set entrySet = map.entrySet();
			Iterator iter = entrySet.iterator();
			while (iter.hasNext()) {
				
				Entry<String, Tuple> entry = (Entry)iter.next();
				iter.remove();

				currKey = entry.getKey();
				prevKey = (prevKey == null) ? currKey : prevKey;
				
				Tuple tuple = (Tuple)entry.getValue();
				List<String> lines = tuple.lines;
				
				for(int i = 0; i < lines.size(); i++) {
					
					if (dist) {  // dump the distribution
						if (!currKey.equals(prevKey)) {
							String str = (numeric) ? Utils.trimLeftZeros(prevKey) : prevKey;
							bw.write(str);
							bw.write("\t");
							bw.write("" + freq);
							bw.write("\n");
							freq = 0;
							prevKey = currKey;
							numberOfDumpedRows++;
						}
						
						freq++;
					}
					else {
						if (extract)
							bw.write((numeric) ? Utils.trimLeftZeros(currKey) : currKey);
						else
							bw.write(lines.get(i));
						
						bw.write("\n");
						numberOfDumpedRows++;
					}
				}
				
				if (tuple.run != null) {
					toLoad = tuple.run;
					break;
				}
			}
			
			if (toLoad != null) {
				for (int i = 0; i < toLoad.length; i++) {
					loadNextPage(toLoad[i]);
				}
			}
		}
		
		if (dist) {
			String str = (numeric) ? Utils.trimLeftZeros(prevKey) : prevKey;
			bw.write(str);
			bw.write("\t");
			bw.write("" + freq);
			bw.write("\n");
			numberOfDumpedRows++;
		}
		
		bw.flush();
		
		// close if it is not the stdout
		if (out != System.out) out.close();
	}
}

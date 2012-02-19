package net.sf.samtools;

/* The MIT License

 Copyright (c) 2010 Broad Institute.

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

/* Contact: Heng Li <hengli@broadinstitute.org> */

import net.sf.samtools.util.BlockCompressedInputStream;

import java.io.*;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import edu.unc.genomics.io.LineReader;
import edu.unc.genomics.io.LineReaderIterator;

public class TabixReader implements LineReader, Iterable<String> {
	protected Path mFn;
	private BlockCompressedInputStream mFp;

	protected int mPreset;
	protected int mSc;
	protected int mBc;
	protected int mEc;
	protected int mMeta;
	protected int mSkip;
	protected String[] mSeq;

	protected HashMap<String, Integer> mChr2tid;

	static final String DEFAULT_INDEX_EXTENSION = ".tbi";
	static final int MAX_BIN = 37450;
	static final int TAD_MIN_CHUNK_GAP = 32768;
	static final int TAD_LIDX_SHIFT = 14;
	
	protected static Path index;

	class TPair64 implements Comparable<TPair64> {
		long u, v;

		public TPair64(final long _u, final long _v) {
			u = _u;
			v = _v;
		}

		public TPair64(final TPair64 p) {
			u = p.u;
			v = p.v;
		}

		public int compareTo(final TPair64 p) {
			return u == p.u ? 0 : ((u < p.u) ^ (u < 0) ^ (p.u < 0)) ? -1 : 1; // unsigned
																																				// 64-bit
																																				// comparison
		}
	};

	class TIndex {
		HashMap<Integer, TPair64[]> b; // binning index
		long[] l; // linear index
	};

	private TIndex[] mIndex;

	class TIntv {
		int tid, beg, end, bin;
	};

	private static boolean less64(final long u, final long v) { // unsigned 64-bit
																															// comparison
		return (u < v) ^ (u < 0) ^ (v < 0);
	}

	/**
	 * The constructor
	 * 
	 * @param fn
	 *          File name of the data file
	 */
	public TabixReader(final Path p) throws IOException {
		mFn = p;
		mFp = new BlockCompressedInputStream(p.toFile());
		
		index = mFn.resolveSibling(mFn.getFileName() + DEFAULT_INDEX_EXTENSION);
		if (Files.exists(index)) {
			readIndex();
		}
	}

	private static int reg2bins(final int beg, final int _end, final int[] list) {
		int i = 0, k, end = _end;
		if (beg >= end)
			return 0;
		if (end >= 1 << 29)
			end = 1 << 29;
		--end;
		list[i++] = 0;
		for (k = 1 + (beg >> 26); k <= 1 + (end >> 26); ++k)
			list[i++] = k;
		for (k = 9 + (beg >> 23); k <= 9 + (end >> 23); ++k)
			list[i++] = k;
		for (k = 73 + (beg >> 20); k <= 73 + (end >> 20); ++k)
			list[i++] = k;
		for (k = 585 + (beg >> 17); k <= 585 + (end >> 17); ++k)
			list[i++] = k;
		for (k = 4681 + (beg >> 14); k <= 4681 + (end >> 14); ++k)
			list[i++] = k;
		return i;
	}

	private static int readInt(final InputStream is) throws IOException {
		byte[] buf = new byte[4];
		is.read(buf);
		return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}

	private static long readLong(final InputStream is) throws IOException {
		byte[] buf = new byte[8];
		is.read(buf);
		return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getLong();
	}

	public static String readLine(final InputStream is) throws IOException {
		StringBuilder buf = new StringBuilder();
		int c;
		while ((c = is.read()) >= 0 && c != '\n')
			buf.append((char)c);
		if (c < 0) return null;
		return buf.toString();
	}

	/**
	 * Read the Tabix index from a file
	 * 
	 * @param fp
	 *          File pointer
	 */
	private void readIndex(final Path p) throws IOException {
		BlockCompressedInputStream is = new BlockCompressedInputStream(p.toFile());
		byte[] buf = new byte[4];

		is.read(buf, 0, 4); // read "TBI\1"
		mSeq = new String[readInt(is)]; // # sequences
		mChr2tid = new HashMap<String, Integer>();
		mPreset = readInt(is);
		mSc = readInt(is);
		mBc = readInt(is);
		mEc = readInt(is);
		mMeta = readInt(is);
		mSkip = readInt(is);
		// read sequence dictionary
		int i, j, k, l = readInt(is);
		buf = new byte[l];
		is.read(buf);
		for (i = j = k = 0; i < buf.length; ++i) {
			if (buf[i] == 0) {
				byte[] b = new byte[i - j];
				System.arraycopy(buf, j, b, 0, b.length);
				String s = new String(b);
				mChr2tid.put(s, k);
				mSeq[k++] = s;
				j = i + 1;
			}
		}
		// read the index
		mIndex = new TIndex[mSeq.length];
		for (i = 0; i < mSeq.length; ++i) {
			// the binning index
			int n_bin = readInt(is);
			mIndex[i] = new TIndex();
			mIndex[i].b = new HashMap<Integer, TPair64[]>();
			for (j = 0; j < n_bin; ++j) {
				int bin = readInt(is);
				TPair64[] chunks = new TPair64[readInt(is)];
				for (k = 0; k < chunks.length; ++k) {
					long u = readLong(is);
					long v = readLong(is);
					chunks[k] = new TPair64(u, v); // in C, this is inefficient
				}
				mIndex[i].b.put(bin, chunks);
			}
			// the linear index
			mIndex[i].l = new long[readInt(is)];
			for (k = 0; k < mIndex[i].l.length; ++k)
				mIndex[i].l[k] = readLong(is);
		}
		// close
		is.close();
	}

	/**
	 * Read the Tabix index from the default file.
	 */
	private void readIndex() throws IOException {
		readIndex(index);
	}

	/**
	 * Read one line from the data file.
	 */
	public String readLine() throws IOException {
		return mFp.readLine();
	}
	
	public Set<String> chromosomes() {
		return mChr2tid.keySet();
	}

	@Override
	public Iterator<String> iterator() {
		return new LineReaderIterator(this);
	}

	protected int chr2tid(final String chr) {
		if (mChr2tid.containsKey(chr))
			return mChr2tid.get(chr);
		else
			return -1;
	}

	/**
	 * Parse a region in the format of "chr1", "chr1:100" or "chr1:100-1000"
	 * 
	 * @param reg
	 *          Region string
	 * @return An array where the three elements are sequence_id, region_begin and
	 *         region_end. On failure, sequence_id==-1.
	 */
	private int[] parseReg(final String reg) { // FIXME: NOT working when the
																						// sequence name contains : or -.
		String chr;
		int colon, hyphen;
		int[] ret = new int[3];
		colon = reg.indexOf(':');
		hyphen = reg.indexOf('-');
		chr = colon >= 0 ? reg.substring(0, colon) : reg;
		ret[1] = colon >= 0 ? Integer.parseInt(reg.substring(colon + 1,
				hyphen >= 0 ? hyphen : reg.length())) - 1 : 0;
		ret[2] = hyphen >= 0 ? Integer.parseInt(reg.substring(hyphen + 1))
				: 0x7fffffff;
		ret[0] = chr2tid(chr);
		return ret;
	}

	protected TIntv getIntv(final String s) {
		TIntv intv = new TIntv();
		int col = 0, end = 0, beg = 0;
		while ((end = s.indexOf('\t', beg)) >= 0 || end == -1) {
			++col;
			if (col == mSc) {
				if (end == -1) {
					intv.tid = chr2tid(s.substring(beg));
				} else {
					intv.tid = chr2tid(s.substring(beg, end));
				}
			} else if (col == mBc) {
				if (end == -1) {
					intv.beg = intv.end = Integer.parseInt(s.substring(beg));
				} else {
					intv.beg = intv.end = Integer.parseInt(s.substring(beg, end));
				}
				if ((mPreset & 0x10000) != 0)
					++intv.end;
				else
					--intv.beg;
				if (intv.beg < 0)
					intv.beg = 0;
				if (intv.end < 1)
					intv.end = 1;
			} else { // FIXME: SAM supports are not tested yet
				if ((mPreset & 0xffff) == 0) { // generic
					if (col == mEc) {
						if (end == -1) {
							intv.end = Integer.parseInt(s.substring(beg));
						} else {
							intv.end = Integer.parseInt(s.substring(beg, end));
						}
					}
				} else if ((mPreset & 0xffff) == 1) { // SAM
					if (col == 6) { // CIGAR
						int l = 0, i, j;
						String cigar = s.substring(beg, end);
						for (i = j = 0; i < cigar.length(); ++i) {
							if (cigar.charAt(i) > '9') {
								int op = cigar.charAt(i);
								if (op == 'M' || op == 'D' || op == 'N')
									l += Integer.parseInt(cigar.substring(j, i));
							}
						}
						intv.end = intv.beg + l;
					}
				} else if ((mPreset & 0xffff) == 2) { // VCF
					String alt;
					alt = end >= 0 ? s.substring(beg, end) : s.substring(beg);
					if (col == 4) { // REF
						if (alt.length() > 0)
							intv.end = intv.beg + alt.length();
					} else if (col == 8) { // INFO
						int e_off = -1, i = alt.indexOf("END=");
						if (i == 0)
							e_off = 4;
						else if (i > 0) {
							i = alt.indexOf(";END=");
							if (i >= 0)
								e_off = i + 5;
						}
						if (e_off > 0) {
							i = alt.indexOf(";", e_off);
							intv.end = Integer.parseInt(i > e_off ? alt.substring(e_off, i)
									: alt.substring(e_off));
						}
					}
				}
			}
			if (end == -1)
				break;
			beg = end + 1;
		}
		return intv;
	}
	
	private TabixIterator query(final int tid, final int beg, final int end) {
		TPair64[] off, chunks;
		long min_off;
		TIndex idx = mIndex[tid];
		int[] bins = new int[MAX_BIN];
		int i, l, n_off, n_bins = reg2bins(beg, end, bins);
		if (idx.l.length > 0)
			min_off = (beg >> TAD_LIDX_SHIFT >= idx.l.length) ? idx.l[idx.l.length - 1]
					: idx.l[beg >> TAD_LIDX_SHIFT];
		else
			min_off = 0;
		for (i = n_off = 0; i < n_bins; ++i) {
			if ((chunks = idx.b.get(bins[i])) != null)
				n_off += chunks.length;
		}
		if (n_off == 0)
			return null;
		off = new TPair64[n_off];
		for (i = n_off = 0; i < n_bins; ++i)
			if ((chunks = idx.b.get(bins[i])) != null)
				for (int j = 0; j < chunks.length; ++j)
					if (less64(min_off, chunks[j].v))
						off[n_off++] = new TPair64(chunks[j]);
		if (n_off == 0)
			return null;
		Arrays.sort(off, 0, n_off);
		// resolve completely contained adjacent blocks
		for (i = 1, l = 0; i < n_off; ++i) {
			if (less64(off[l].v, off[i].v)) {
				++l;
				off[l].u = off[i].u;
				off[l].v = off[i].v;
			}
		}
		n_off = l + 1;
		// resolve overlaps between adjacent blocks; this may happen due to the
		// merge in indexing
		for (i = 1; i < n_off; ++i)
			if (!less64(off[i - 1].v, off[i].u))
				off[i - 1].v = off[i].u;
		// merge adjacent blocks
		for (i = 1, l = 0; i < n_off; ++i) {
			if (off[l].v >> 16 == off[i].u >> 16)
				off[l].v = off[i].v;
			else {
				++l;
				off[l].u = off[i].u;
				off[l].v = off[i].v;
			}
		}
		n_off = l + 1;
		// return
		TPair64[] ret = new TPair64[n_off];
		for (i = 0; i < n_off; ++i)
			ret[i] = new TPair64(off[i].u, off[i].v); // in C, this is inefficient
		return new TabixReader.TabixIterator(tid, beg, end, ret);
	}
	
	/**
	 * Query for records in this file
	 * @param chr
	 * @param beg
	 * @param end
	 * @return
	 */
	public TabixIterator query(final String chr, final int beg, final int end) {
		return query(chr2tid(chr), beg, end);
	}

	/**
	 * Parse a query string of the form chrVI:1-1000
	 * @param reg the query string region
	 * @return
	 */
	public TabixIterator query(final String reg) {
		int[] x = parseReg(reg);
		return query(x[0], x[1], x[2]);
	}
	
	

	/**
	 * @author timpalpant
	 *
	 */
	public class TabixIterator implements Iterator<String> {
		private int i;
		private int tid, beg, end;
		private TPair64[] off;
		private long curr_off;
		private boolean iseof;
		private String nextLine;

		public TabixIterator(final int _tid, final int _beg, final int _end,
				final TPair64[] _off) {
			i = -1;
			curr_off = 0;
			iseof = false;
			off = _off;
			tid = _tid;
			beg = _beg;
			end = _end;
			advance();
		}
		
		@Override
		public boolean hasNext() {
			return !iseof;
		}

		@Override
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Cannot remove records from Tabix files");
		}

		@Override
		public String next() {
			String r = nextLine;
			advance();
			return r;
		}
		
		private void advance() throws NoSuchElementException {
			if (iseof) {
				return;
			}
			
			nextLine = null;
			for (;;) {
				if (curr_off == 0 || !less64(curr_off, off[i].v)) { // then jump to the next chunk
					if (i == off.length - 1) {
						iseof = true;
						break; // no more chunks
					}
					
					if (i >= 0) {
						assert (curr_off == off[i].v); // otherwise bug
					}
					
					if (i < 0 || off[i].v != off[i + 1].u) { // not adjacent chunks; then seek
						try {
							mFp.seek(off[i + 1].u);
						} catch (IOException e) {
							throw new NoSuchElementException("IOException while trying to get next element");
						}
						curr_off = mFp.getFilePointer();
					}
					
					++i;
				}
				
				String s;
				try {
					if ((s = mFp.readLine()) != null) {
						TIntv intv;
						char[] str = s.toCharArray();
						curr_off = mFp.getFilePointer();
						if (str.length == 0 || str[0] == mMeta) {
							continue;
						}
						intv = getIntv(s);
						if (intv.tid != tid || intv.beg >= end) {
							iseof = true;
							break; // no need to proceed
						} else if (intv.end > beg && intv.beg < end) {
							nextLine = s; // overlap; return
							return;
						}
					} else {
						iseof = true;
						break;
					}
				} catch (IOException e) {
					throw new NoSuchElementException("IOException while trying to get next element");
				}
			}
		}
	}

}

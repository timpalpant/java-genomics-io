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

import it.unimi.dsi.mg4j.io.FastByteArrayOutputStream;
import it.unimi.dsi.mg4j.util.MutableString;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.regex.Pattern;
import java.util.zip.Deflater;

/**
 * Utility class with a variety of multi-purpose static methods.
 * 
 * @author Claudio Corsi
 * @author Paolo Ferragina
 *
 */
public abstract class Utils {
	
	private static ByteBuffer bb = ByteBuffer.allocateDirect(8);
	
	private static double giga = Math.pow(2, 30);
	private static double mega = Math.pow(2, 20);
	private static double kilo = Math.pow(2, 10);
	
	private static int[] offsets = new int[1000000];	// max 1M cols allowed!		  4 MB
	private static char[] chars = new char[100000];	// max 100.000 chars per String!  200 KB
	private static byte[] buff = new byte[(int)Math.pow(2, 10) * 16];				// 16 KB
	
	private Utils() {}
	
	public static double round(double val, int precision) {
		return (double)Math.round(val * precision) / precision; 
	}
	
	public static String formatSize(long size) {
		if (size >= giga) return round(size/giga, 10) + "GB";
		if (size >= mega) return round(size/mega, 10) + "MB";
		if (size >= kilo) return round(size/kilo, 10) + "KB";
		return size + " bytes";
	}
	
	public static String elapsedTime(long start, long stop) {
		return elapsedTime(stop - start);
	}
	
	public static String elapsedTime(long msecs) {
		int timeInSeconds = (int)(msecs) / 1000;
		int hours, minutes, seconds;
		hours = timeInSeconds / 3600;
		timeInSeconds = timeInSeconds - (hours * 3600);
		minutes = timeInSeconds / 60;
		timeInSeconds = timeInSeconds - (minutes * 60);
		seconds = timeInSeconds;
		return hours + "h " + minutes + "min " + seconds + "sec";
	}
	
	public static byte[] intToBytes(int val) {
		
	    return new byte[] {
                (byte)(val >>> 24),
                (byte)(val >>> 16),
                (byte)(val >>> 8),
                (byte)val};
	    
	}
	
	public static byte[] longToBytes(long val) {
		bb.clear();
		bb.putLong(val);
		bb.flip();
	    byte[] byteData = new byte[8];
	    bb.get(byteData);
	    
	    return byteData;
	}
	
	public static byte[] merge(byte[] b1, byte[] b2) {

		byte[] buff = new byte[b1.length + b2.length];
		System.arraycopy(b1, 0, buff, 0, b1.length);
		System.arraycopy(b2, 0, buff, b1.length, b2.length);
		
		return buff;
	}
	
	public static long bytesToLong(byte[] array) {
		bb.clear();
		bb.put(array);
		bb.flip();
	    
	    return bb.getLong();
	}
	
	public static int bytesToInt(byte[] array) {
		
		int i = (0xff & array[0]) << 24;
		i |= (0xff & array[1]) << 16;
		i |= (0xff & array[2]) << 8;
		i |= (0xff & array[3]);
		return i;
	}
	
	/**
	 * Compress with ZIP using the java.util.zip package.
	 * 
	 * @deprecated
	 * @see #fastZip(byte[], int, int, FastByteArrayOutputStream, int)
	 */
	public static byte[] zip(byte[] input, int off, int len) {

		// Create the compressor with highest level of compression
		Deflater compressor = new Deflater();
		compressor.setLevel(Deflater.BEST_COMPRESSION);

		// Give the compressor the data to compress
		compressor.setInput(input, off, len);
		compressor.finish();

		/*
		 * Create an expandable byte array to hold the compressed data.
		 * You cannot use an array that's the same size as the orginal because
		 * there is no guarantee that the compressed data will be smaller than
		 * the uncompressed data.
		 */
		ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

		// Compress the data
		byte[] buf = new byte[1024];
		while (!compressor.finished()) {
			int count = compressor.deflate(buf);
			bos.write(buf, 0, count);
		}

		compressor.end();

		// Get the compressed data
		return bos.toByteArray();
	}
	
	/**
	 * A fast way to load in memory the content of a file.
	 * 
	 * @param file the file to load
	 * @return the file content in bytes
	 * @throws IOException
	 */
	public static byte[] loadFromDisk(String file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		FileChannel ch = fis.getChannel();
		ByteBuffer buff = ByteBuffer.allocate((int)ch.size());
		ch.read(buff);
		fis.close();
		ch.close();
		return buff.array();
	}
	
	public static String getSortingCols(String line, int[] fields, Pattern p) {
		
		if (fields == null || fields.length == 0) return line;
		
		String[] a = p.split(line);
		StringBuffer sb = new StringBuffer();
		
		int i = 0;
		for(; i < fields.length - 1; i++) {
			sb.append(a[fields[i]]);
			sb.append(p.pattern());
		}
		sb.append(a[fields[i]]);
			
		return sb.toString();
	}
	
	public static String trimLeftZeros(String str) {
		int i = 0;
		for(; i < str.length(); i++) {
			if (str.charAt(i) != '0') break;
		}
		return str.substring(i);
	}
	
	// WARNING: columns size must be less or equals to 100.000 chars!!
	public static String getKey(String line, int[] cols, char sep, boolean numeric) {
		
		if (cols.length == 0) {			
			return (numeric) ? pad(line) : line;
		}
		
		int j = 1;
		offsets[0] = -1;
		for(int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == sep) {
				offsets[j] = i;
				j++;
			}
		}
		offsets[j] = line.length();
		
		int n = 0;
		for(int i = 0; i < cols.length; i++) {
			
			if (cols[i] > j) continue;	// ignore columns out of range
			
			int start = offsets[cols[i]] + 1;
			int stop = cols[i] + 1 > j ? -1 : offsets[cols[i] + 1];
			
			if (numeric) {
				int dim = stop - start;
				if (dim < 20) {	// padding for 20 chars (signed long)
					int padding = 20 - dim;
					int limit = n + padding;
					for(; n < limit; n++) chars[n] = '0'; 
				}
			}
			
			for(int k = start; k < stop; k++) {
				chars[n] = line.charAt(k);
				n++;
			}
			chars[n] = sep;
			n++;
		}
		
		if (n == 0) return "";
		
		return new String(chars, 0, n-1);
	}
	
	public static String pad(String str) {
		int n = 0;
		for(; n < 20 - str.length(); n++) chars[n] = '0';
		int limit = n + str.length();
		int i = 0;
		for(; n < limit; n++) chars[n] = str.charAt(i++);
		
		return new String(chars, 0, n);
	}
	
	public static long getLong(byte[] buff, int pos) {
		return (
				((long)(buff[pos] & 0xff) << 56) |
				((buff[pos+1] & 0xff) << 48) |
				((buff[pos+2] & 0xff) << 40) |
				((buff[pos+3] & 0xff) << 32) |
				((buff[pos+4] & 0xff) << 24) |
				((buff[pos+5] & 0xff) << 16) |
				((buff[pos+6] & 0xff) <<  8) |
				((buff[pos+7] & 0xff) <<  0));
	}
	
	public static MutableString getField(MutableString rec, int field, MutableString fieldSeparator) {
		
		if (field == -1) return rec;
		
		int l = fieldSeparator.length();
		int start = 0;
		int end = 0;
		int k = -1;
		for (; k < field; k++) {
			
			start = end;
			
			end = rec.indexOf(fieldSeparator, end);
			
			if (end == -1) {
				end = rec.length();				
			}
			else
				end += l; // skip the found pattern
		}
		
		if (start == end) return null;
		
		return rec.substring(start, end);
	}
}

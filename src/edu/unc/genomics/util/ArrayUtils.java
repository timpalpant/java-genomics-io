/**
 * 
 */
package edu.unc.genomics.util;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author timpalpant
 *
 */
public class ArrayUtils {
	public static <T> String join(Collection<T> list, String delimeter) {
		StringBuilder s = new StringBuilder();
		for (T element : list) {
			s.append(element).append(delimeter);
		}
		return s.toString();
	}
	
	public static <T> String join(T[] list, String delimeter) {
		return join(Arrays.asList(list), delimeter);
	}
	
	public static String join(float[] list, String delimeter) {
		StringBuilder s = new StringBuilder();
		for (float element : list) {
			s.append(element).append(delimeter);
		}
		return s.toString();
	}
	
	public static String join(double[] list, String delimeter) {
		StringBuilder s = new StringBuilder();
		for (double element : list) {
			s.append(element).append(delimeter);
		}
		return s.toString();
	}
	
	public static String join(int[] list, String delimeter) {
		StringBuilder s = new StringBuilder();
		for (int element : list) {
			s.append(element).append(delimeter);
		}
		return s.toString();
	}
	
	public static String join(long[] list, String delimeter) {
		StringBuilder s = new StringBuilder();
		for (long element : list) {
			s.append(element).append(delimeter);
		}
		return s.toString();
	}
	
	public static int[] mapToInt(String[] list) {
		int[] ret = new int[list.length];
		for (int i = 0; i < list.length; i++) {
			ret[i] = Integer.parseInt(list[i]);
		}
		return ret;
	}
	
	public static float[] add(float[] v1, float[] v2) {
		if (v1.length != v2.length) {
			throw new IllegalArgumentException("Cannot add arrays of unequal length");
		}
		
		float[] result = new float[v1.length];
		for (int i = 0; i < v1.length; i++) {
			result[i] = v1[i] + v2[i];
		}
		
		return result;
	}
}

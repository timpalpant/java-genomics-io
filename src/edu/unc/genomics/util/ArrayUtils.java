/**
 * 
 */
package edu.unc.genomics.util;

/**
 * @author timpalpant
 *
 */
public class ArrayUtils {
	
	public static int[] mapToInt(String[] list) {
		int[] ret = new int[list.length];
		for (int i = 0; i < list.length; i++) {
			ret[i] = Integer.parseInt(list[i]);
		}
		return ret;
	}
	
	public static float[] mapToFloat(String[] list) {
		float[] ret = new float[list.length];
		for (int i = 0; i < list.length; i++) {
			ret[i] = Float.parseFloat(list[i]);
		}
		return ret;
	}
}

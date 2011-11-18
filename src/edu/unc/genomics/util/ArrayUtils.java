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
	public static String join(Collection<String> list, String delimeter) {
		StringBuilder s = new StringBuilder();
		for (String str : list) {
			s.append(str).append(delimeter);
		}
		return s.toString();
	}
	
	public static String join(String[] list, String delimeter) {
		return join(Arrays.asList(list), delimeter);
	}
}

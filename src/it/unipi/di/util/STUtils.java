package it.unipi.di.util;

/**
 * Extracted from SmallText / TextDB
 * @author timpalpant
 *
 */
public class STUtils {
	
	private static int[] offsets = new int[1000000];	// max 1M cols allowed!		  4 MB
	private static char[] chars = new char[100000];	// max 100.000 chars per String!  200 KB
	
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
	
	public static String trimLeftZeros(String str) {
		int i = 0;
		for(; i < str.length(); i++) {
			if (str.charAt(i) != '0') break;
		}
		return str.substring(i);
	}
	
}

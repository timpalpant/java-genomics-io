package edu.unc.util;

/**
 * @author timpalpant
 *
 */
public class NumberUtils {
	public static boolean isInteger(String str) {  
	  try {  
	    Integer.parseInt(str); 
	  } catch(NumberFormatException nfe) {  
	    return false;  
	  }
	  
	  return true;  
	}
	
	public static boolean isNumeric(String str) {  
	  try {  
	    Double.parseDouble(str);  
	  } catch(NumberFormatException nfe) {  
	    return false;  
	  }
	  
	  return true;  
	}
}

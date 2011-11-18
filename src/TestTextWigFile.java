import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import edu.unc.genomics.Interval;
import edu.unc.genomics.io.WigFile;
import edu.unc.genomics.io.WigFileException;


public class TestTextWigFile {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws WigFileException 
	 */
	public static void main(String[] args) throws IOException, WigFileException {
		Path p = Paths.get("/Users/timpalpant/Desktop/Lab/DY150-dyads.wig");
		
		long timerStart = System.currentTimeMillis();
		WigFile wig = WigFile.autodetect(p);
		long indexingTime = (System.currentTimeMillis() - timerStart);
		
		// Print out summary
		System.out.println(wig.toString());
		
		// Attempt to query the wig file
		String chr = "chrV";
		int start = 10_000;
		int stop = 10_100;
		Interval interval = new Interval(chr, start, stop);
		timerStart = System.currentTimeMillis();
		float[] result = wig.query(interval);
		long queryTime = (System.currentTimeMillis() - timerStart);
		for (int i = start; i <= stop; i++) {
			System.out.println(i + ": " + result[i-start]);
		}
		
		System.out.println("Indexing time: " + indexingTime);
		System.out.println("Query time: " + queryTime);
	}

}

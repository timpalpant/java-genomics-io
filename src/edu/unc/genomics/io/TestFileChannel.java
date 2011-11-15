package edu.unc.genomics.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.channels.FileChannel;
import java.util.Scanner;

public class TestFileChannel {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Path p = Paths.get("/Users/timpalpant/Desktop/Lab/DY150-dyads.wig");
		FileChannel fc = (FileChannel)Files.newByteChannel(p);
		Scanner scanner = new Scanner(fc);
		
		for (int i = 0; i < 5; i++)
			System.out.println(scanner.nextLine());

		fc.position(0);
		scanner = new Scanner(fc);
		System.out.println(scanner.nextLine());
	}

}

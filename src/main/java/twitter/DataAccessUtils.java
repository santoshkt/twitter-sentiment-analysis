package twitter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class DataAccessUtils {
	public static boolean writeToDatabase(Tweet tweet){
		
		return false;
	}
	public static boolean writeToFile(Tweet tweet){
		
		return false;
	}
	public static boolean writeToFile(ArrayList<Tweet> tweetList, String keyword) throws IOException{
		System.out.println("Writing to output file: "+keyword);
		File outputFile = new File(keyword.replaceAll(" ", "_"));
		if (!outputFile.exists()) {
			outputFile.createNewFile();
		}

		BufferedWriter out;
		FileWriter fileStream = new FileWriter(outputFile, true);
		out = new BufferedWriter(fileStream);
		for(Tweet tweet : tweetList){
			out.write(tweet.toString() + "\n");
		}
		out.close();
		return false;
	}
	
}

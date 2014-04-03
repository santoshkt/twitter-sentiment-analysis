package twitter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import twitter4j.ResponseList;
import twitter4j.User;

public class DataAccessUtils {
	public static boolean writeToDatabase(ResponseList<User> users){
		
		return false;
	}
	public static boolean writeToFile(Tweet tweet){
		
		return false;
	}
	public static boolean writeToFile(ArrayList<Tweet> tweetList, String keyword) throws IOException{
		System.out.println("Writing to output file: "+keyword);
		File outputFile = new File("output/"+keyword.replaceAll(" ", "_"));
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

	public static void writeToFile(ArrayList<CrawledStatus> statuses) {
		// TODO Auto-generated method stub
		
	}
	public static void writeUsersToFile(HashSet<User> userList) {
		// TODO Auto-generated method stub
		
	}
	public static void writeStatusToFile(ArrayList<CrawledStatus> statuses) {
		// TODO Auto-generated method stub
		
	}
	public static void writeFollowersToFile(Long subject, Long followingId) {
		// TODO Auto-generated method stub
		
	}
	
}

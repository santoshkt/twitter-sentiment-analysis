package twitter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import twitter4j.ResponseList;
import twitter4j.User;

public class DataAccessUtils {

	private static Logger logger = LogManager
			.getFormatterLogger(DataAccessUtils.class.getName());

	public static boolean writeToDatabase(ResponseList<User> users) {

		return false;
	}

	public static boolean writeToFile(Tweet tweet) {

		return false;
	}

	public static boolean writeToFile(ArrayList<Tweet> tweetList, String keyword)
			throws IOException {
		System.out.println("Writing to output file: " + keyword);
		File outputFile = new File("output/" + keyword.replaceAll(" ", "_"));
		if (!outputFile.exists()) {
			outputFile.createNewFile();
		}

		BufferedWriter out;
		FileWriter fileStream = new FileWriter(outputFile, true);
		out = new BufferedWriter(fileStream);
		for (Tweet tweet : tweetList) {
			out.write(tweet.toString() + "\n");
		}
		out.close();
		return true;
	}

	public static void writeToFile(HashMap<User, HashSet<User>> followsMap)
			throws IOException {
		logger.entry();
		new File("output").mkdirs();
		File outputFile = new File("output/" + "followsMap.txt");
		if (!outputFile.exists()) {
			outputFile.createNewFile();
		}

		BufferedWriter out;
		FileWriter fileStream = new FileWriter(outputFile, true);
		out = new BufferedWriter(fileStream);

		for (Map.Entry<User, HashSet<User>> entry : followsMap.entrySet()) {

			User user = entry.getKey();
			HashSet<User> hs = entry.getValue();
			out.write("key:" + cleanText(user.toString()) + "\n");

			for (User fuser : hs) {
				out.write("follows:" + cleanText(fuser.toString()) + "\n");
			}
		}

		out.close();
		logger.exit("Writing to file complete.");
		
	}

	public static void writeToFile(Long subject, HashSet<Long> friendsHs)
			throws IOException {

		logger.entry();

		new File("output").mkdirs();
		File outputFile = new File("output/" + "friendsHs.txt");
		if (!outputFile.exists()) {
			outputFile.createNewFile();
		}

		BufferedWriter out;
		FileWriter fileStream = new FileWriter(outputFile, true);
		out = new BufferedWriter(fileStream);

		for (Long friend : friendsHs) {
			out.write(subject + "," + friend + "\n");
		}

		out.close();

		logger.exit("Writing to file complete.");

	}

	public static void writeToFile(ArrayList<CrawledStatus> statuses)
			throws IOException {

		logger.entry();

		new File("output").mkdirs();
		File outputFile = new File("output/" + "statuses.txt");
		if (!outputFile.exists()) {
			outputFile.createNewFile();
		}

		BufferedWriter out;
		FileWriter fileStream = new FileWriter(outputFile, true);
		out = new BufferedWriter(fileStream);

		for (CrawledStatus status : statuses) {
			out.write(cleanText(status.toString()) + "\n");
		}

		out.close();

		logger.exit("Writing to file complete.");
	}
	
	private static String cleanText(String text) {
		text = text.replace("\n", "");
		text = text.replace("\t", " ");
		text = text.replace("\"", "");
		return text;
	}

}

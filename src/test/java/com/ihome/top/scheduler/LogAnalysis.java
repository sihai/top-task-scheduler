package com.ihome.top.scheduler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * D:\home\sihai\scheduler.log "Do job: jobKey = test_group_0.job2_0, jobConent = content:job2_0"
 * @author sihai
 *
 */
public class LogAnalysis {

	static String PREFIX = "D:\\home\\sihai\\analysis\\log-segment";
	static String SUFFIX = "log";
	static int currentSize = 0;
	static int MAX_SIZE = 1000000;
	static BufferedWriter writer = null;
	
	public static void main(String[] args) {
		if(args.length != 2) {
			System.out.println("Error usage");
			return;
		}
		
		int total = 0;
		int count = 0;
		String line = null;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(args[0]));
			while((line = reader.readLine()) != null) {
				if(line.equals(args[1])) {
					count++;
				}
				total++;
				write(line);
			}
			
			if(writer != null) {
				writer.flush();
				writer.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		System.out.println("total = " + total + ", count = " + count);
	}
	
	public static void write(String line) throws IOException {
		if(currentSize % MAX_SIZE == 0) {
			newFile(new StringBuilder(PREFIX).append("-").append(currentSize / MAX_SIZE).append(".").append(SUFFIX).toString());
		}
		writer.write(line);
		writer.newLine();
		currentSize++;
	}
	
	public static void newFile(String fileName) throws IOException {
		if(writer != null) {
			writer.flush();
			writer.close();
		}
		writer = new BufferedWriter(new FileWriter(fileName));
	}
}

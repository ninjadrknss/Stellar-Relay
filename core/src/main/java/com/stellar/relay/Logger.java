package com.stellar.relay;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

public class Logger {
	record LogEntry(int sessionId, Date start, long duration, int score, String difficulty) {
		@Override
		public String toString() {
			return sessionId + "," + start + "," + duration + "," + score + "," + difficulty + "\n";
		}
	}

	private static Date startTime;

	private static int sessionId = 0;

	private static FileWriter writer;

	public static void start() {
		if (writer == null) {
			try {
				boolean isNewFile = !(new File("game_log.csv")).exists();
				writer = new FileWriter("game_log.csv", true);
				// Write header if file is new
				if (isNewFile) writer.write("Session ID,Start Time,Duration (s),Score,Difficulty\n");
				else {
					Scanner scanner = new Scanner(new File("game_log.csv"));
					String line = "";
					while (scanner.hasNextLine()) {
						line = scanner.nextLine();
					}
					sessionId = Integer.parseInt(line.split(",")[0]);
				}
			} catch (Exception e) {
				System.err.println("Failed to initialize logger: " + e.getMessage());
				return;
			}
		}

		startTime = new Date();
		sessionId++;
		System.out.println("Session " + sessionId + " started at " + startTime);
	}

	public static void end() {
		Date endTime = new Date();
		long duration = endTime.getTime() - startTime.getTime();

		LogEntry entry =
				new LogEntry(sessionId, startTime, duration / 1000, GUI.score, Main.difficulty.toString());

		for (int i = 0; i < 5; i++) {
			System.out.println("Logging attempt " + (i + 1) + ": " + entry);
			try {
				writer.write(entry.toString());
				writer.flush();
				System.out.println("Log entry written successfully.");
				break;
			} catch (IOException e) {
				System.err.println("Failed to write log entry: " + e.getMessage());
				if (i == 4) {
					System.err.println("All logging attempts failed. Giving up.");
				}
			}
		}
	}
}

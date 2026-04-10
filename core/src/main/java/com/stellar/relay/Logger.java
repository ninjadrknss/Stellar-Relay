package com.stellar.relay;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class Logger {
	public record LogEntry(int sessionId, Date start, long duration, int score, String difficulty)
			implements Comparable<LogEntry> {
		@Override
		public String toString() {
			return sessionId
					+ ","
					+ start.toString()
					+ ","
					+ duration
					+ ","
					+ score
					+ ","
					+ difficulty
					+ "\n";
		}

		public static LogEntry fromCSV(String csvLine) {
			String[] parts = csvLine.split(",");
			try {
				return new LogEntry(
						Integer.parseInt(parts[0]),
						new Date(Date.parse(parts[1])),
						Long.parseLong(parts[2]),
						Integer.parseInt(parts[3]),
						parts[4]);
			} catch (NumberFormatException e) {
				System.err.println("Failed to parse number in log entry from CSV: " + e.getMessage());
				return null;
			} catch (ArrayIndexOutOfBoundsException e) {
				System.err.println("Invalid log entry format in CSV: " + csvLine);
				return null;
			}
		}

		@Override
		public int compareTo(LogEntry other) {
			return this.score - other.score;
		}
	}

	public static final ArrayList<LogEntry> logList = new ArrayList<>(); // Max-heap based on duration

	private static Date startTime;

	private static int sessionId = 0;

	public static boolean isActive = false;

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
						LogEntry entry = LogEntry.fromCSV(line);
						if (entry != null) logList.add(entry);
					}
					sessionId = Integer.parseInt(line.split(",")[0]);
					scanner.close();
				}
			} catch (NumberFormatException e) {
				System.err.println("Failed to parse session ID from log file: " + e.getMessage());
				return;
			} catch (Exception e) {
				System.err.println("Failed to initialize logger: " + e.getMessage());
				return;
			}

			logList.sort((a, b) -> Long.compare(b.duration, a.duration));
		}
		if (isActive) {
			System.err.println("Logger is already active. Cannot start a new session.");
			return;
		}

		startTime = new Date();
		sessionId++;
		isActive = true;
		System.out.println("Session " + sessionId + " started at " + startTime);
	}

	public static void end() {
		if (!isActive) {
			System.err.println("Logger is not active. Cannot end session.");
			return;
		}

		isActive = false;

		Date endTime = new Date();
		long duration = endTime.getTime() - startTime.getTime();

		LogEntry entry =
				new LogEntry(sessionId, startTime, duration / 1000, GUI.score, Main.difficulty.toString());

		logList.add(entry);
		logList.sort((a, b) -> Long.compare(b.duration, a.duration));

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

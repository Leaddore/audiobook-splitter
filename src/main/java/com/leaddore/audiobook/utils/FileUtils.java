package com.leaddore.audiobook.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.leaddore.audiobook.splitter.range.Range;

/**
 * The Class FileUtils.
 */
public final class FileUtils {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(FileUtils.class);

	/** The Constant OUTPUT_FILE_NAME. */
	public static final String OUTPUT_FILE_NAME = "timecodes";

	public static final String FFMPEG_LOG_NAME = "ffmpeg.log";

	/**
	 * Instantiates a new file utils.
	 */
	private FileUtils() {

		// Static class, no need for constructing.

	}

	public static final void writeToFfmpegLog(String logLine) {

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(FFMPEG_LOG_NAME, true))) {

			writer.write(logLine);
			writer.write("\n\r");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Write timecode file.
	 *
	 * @param ranges the ranges
	 */
	public static final void writeTimecodeFile(List<Range> ranges) {

		StringBuilder sb = new StringBuilder();

		ranges.stream().forEach(s -> sb.append(s.getStartTime()).append("-").append(s.getStopTime()).append("-")
				.append(s.getTitle()).append("\n"));

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE_NAME))) {

			writer.write(sb.toString());

		} catch (IOException e) {

			LOGGER.error("Unable to write time codes to file {}", OUTPUT_FILE_NAME);
			e.printStackTrace();
		}

	}

	/**
	 * Read ranges file.
	 *
	 * @return the list
	 */
	public static final List<Range> readRangesFile() {

		List<Range> ranges = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(OUTPUT_FILE_NAME))) {

			ranges = reader.lines().map(line -> {
				String[] fields = line.split("-");
				Range r = new Range();
				r.setStartTime(fields[0]);
				r.setStopTime(fields[1]);
				r.setTitle(fields[2]);
				return r;
			}).collect(Collectors.toList());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ranges;

	}

}

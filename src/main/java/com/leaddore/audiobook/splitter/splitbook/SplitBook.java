package com.leaddore.audiobook.splitter.splitbook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.leaddore.audiobook.splitter.range.Range;
import com.leaddore.audiobook.utils.FileUtils;

/**
 * The Class SplitBook.
 */
public class SplitBook {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(SplitBook.class);

	/** The range. */
	private List<Range> range;

	/** The audio book path. */
	private String audioBookPath = "";

	/** The ffmpeg path. */
	private String ffmpegPath = "";

	/**
	 * Instantiates a new split book.
	 *
	 * @param range         the range
	 * @param audioBookPath the audio book path
	 * @param ffmpegPath    the ffmpeg path
	 */
	public SplitBook(List<Range> range, String audioBookPath, String ffmpegPath) {

		setRange(range);
		setAudioBookPath(audioBookPath);
		setFfmpegPath(ffmpegPath);

	}

	/**
	 * Gets the range.
	 *
	 * @return the range
	 */
	public List<Range> getRange() {
		return range;
	}

	/**
	 * Sets the range.
	 *
	 * @param range the new range
	 */
	public void setRange(List<Range> range) {
		this.range = range;
	}

	/**
	 * Split it up.
	 */
	public void splitItUp() {

		for (int i = 0; i < range.size(); i++) {

			Range timeRange = range.get(i);

			splitMp3(timeRange, i);

		}

	}

	/**
	 * Split mp 3.
	 *
	 * @param timeRange the time range
	 * @param i         the i
	 */
	private void splitMp3(Range timeRange, int i) {

		List<String> command = generateCommand(timeRange, i);
		LOGGER.warn("Sending command to ffmpeg: {}", command);

		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectErrorStream(true);

		try {
			Process process = pb.start();

			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;

			while ((line = br.readLine()) != null) {

				FileUtils.writeToFfmpegLog(line);
			}

			process.waitFor();

			LOGGER.warn("Segment Created: {}", audioBookPath + " " + timeRange.getTitle() + ".mp3");

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Generate command.
	 *
	 * @param timeRange the time range
	 * @param i         the i
	 * @return the list
	 */
	private List<String> generateCommand(Range timeRange, int i) {

		LOGGER.warn(timeRange.getStartTime());
		LOGGER.warn(timeRange.getStopTime());
		LOGGER.warn(timeRange.getTitle());

		List<String> command = new ArrayList<>();
		command.add(ffmpegPath);
		command.add("-i");
		command.add(audioBookPath);
		command.add("-ss");
		command.add(timeRange.getStartTime());
		if ((!"null".equals(timeRange.getStopTime())) || null == timeRange.getStopTime()) {
			command.add("-to");
			command.add(timeRange.getStopTime());
		}
		command.add("-c");
		command.add("copy");
		command.add(audioBookPath + timeRange.getTitle() + ".mp3");

		return command;
	}

	/**
	 * Gets the audio book path.
	 *
	 * @return the audio book path
	 */
	public String getAudioBookPath() {
		return audioBookPath;
	}

	/**
	 * Sets the audio book path.
	 *
	 * @param audioBookPath the new audio book path
	 */
	public void setAudioBookPath(String audioBookPath) {
		this.audioBookPath = audioBookPath;
	}

	/**
	 * Gets the ffmpeg path.
	 *
	 * @return the ffmpeg path
	 */
	public String getFfmpegPath() {
		return ffmpegPath;
	}

	/**
	 * Sets the ffmpeg path.
	 *
	 * @param ffmpegPath the new ffmpeg path
	 */
	public void setFfmpegPath(String ffmpegPath) {
		this.ffmpegPath = ffmpegPath;
	}

}

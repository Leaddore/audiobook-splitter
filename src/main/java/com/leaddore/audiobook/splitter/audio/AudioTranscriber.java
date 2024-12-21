package com.leaddore.audiobook.splitter.audio;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import com.leaddore.audiobook.splitter.range.Range;

/**
 * The Class AudioTranscriber.
 */
public class AudioTranscriber {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(AudioTranscriber.class);

	/** The Constant BUFFER_SIZE. */
	private static final int BUFFER_SIZE = 8192;

	/** The ffmpeg location. */
	private String ffmpegLocation = "";

	/** The audio book location. */
	private String audioBookLocation = "";

	/** The file name. */
	private String fileName = "";

	/** The wave name. */
	private String waveName = "";

	/**
	 * Instantiates a new audio transcriber.
	 *
	 * @param ffmpegLocation    the ffmpeg location
	 * @param audioBookLocation the audio book location
	 */
	public AudioTranscriber(String ffmpegLocation, String audioBookLocation) {
		this.ffmpegLocation = ffmpegLocation;
		this.audioBookLocation = audioBookLocation;

		Path path = Paths.get(audioBookLocation);

		fileName = path.getFileName().toString();
	}

	/**
	 * Transcribe.
	 *
	 * @return the list
	 * 
	 *         Returns a list of ranges of time codes from reading the audio book
	 *         file.
	 * 
	 * @see {@link Range}
	 */
	public List<Range> transcribe() {

		List<String> wordList = new ArrayList<>();
		List<Range> timeCodes = new ArrayList<>();

		wordList.add("chapter");

		LibVosk.setLogLevel(LogLevel.WARNINGS);

		if (!audioBookLocation.endsWith(".wav")) {
			convertToWav();
		} else {

			setWaveName(fileName);

		}

		try (Model model = new Model("model");
				InputStream ais = AudioSystem
						.getAudioInputStream(new BufferedInputStream(new FileInputStream(waveName)));
				Recognizer recognizer = new Recognizer(model, getWavSampleRate())) {

			recognizer.setWords(true);

			int nbytes;
			byte[] b = new byte[BUFFER_SIZE];
			while ((nbytes = ais.read(b)) >= 0) {

				if (recognizer.acceptWaveForm(b, nbytes)) {

					String result = recognizer.getFinalResult();

					JSONObject jsonObject = new JSONObject(result);

					if (jsonObject.has("result")) {
						JSONArray jsonArray = new JSONArray(jsonObject.getJSONArray("result"));

						for (int i = 0; i < jsonArray.length(); i++) {

							JSONObject obj = jsonArray.getJSONObject(i);

							if (wordList.contains(obj.getString("word"))) {

								BigDecimal startTime = obj.getBigDecimal("start");

								String convertedTime = convertToTimeFormat(startTime);

								if (timeCodes.isEmpty()) {

									Range range = new Range();

									range.setStartTime("00:00:00");
									range.setStopTime(convertedTime);
									LOGGER.warn("New timecode added: {}", range);
									timeCodes.add(range);
									Range range2 = new Range();
									range2.setStartTime(convertedTime);
									timeCodes.add(range2);

								} else {

									timeCodes.get(timeCodes.size() - 1).setStopTime(convertedTime);
									LOGGER.warn("New timecode added: {}", timeCodes.get(timeCodes.size() - 1));
									Range range = new Range();
									range.setStartTime(convertedTime);
									timeCodes.add(range);

								}

							}

						}
					}

				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
		}
		return timeCodes;
	}

	/**
	 * Gets the wav sample rate.
	 *
	 * @return the wav sample rate
	 */
	private float getWavSampleRate() {
		float sampleRate = 0;
		try {
			AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(new File(waveName));

			AudioFormat format = fileFormat.getFormat();

			sampleRate = format.getSampleRate();
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
		return sampleRate;
	}

	/**
	 * Convert to wav.
	 */
	private void convertToWav() {
		String newWavName = fileName.split("\\.")[0] + ".wav";

		List<String> command = createCommand(newWavName);

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectErrorStream(true);

		Process process = null;
		try {
			process = processBuilder.start();

			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			String duration = "";
			String currentTime = "";

			while ((line = br.readLine()) != null) {

				if (line.contains("Duration:")) {
					Scanner scanner = new Scanner(line);

					while (scanner.hasNext()) {
						String token = scanner.next();

						if ("Duration:".equals(token) && scanner.hasNext()) {
							duration = scanner.next().replace(",", "");
						}

					}
					scanner.close();

				} else if (line.contains("time=")) {

					Scanner scanner = new Scanner(line);

					while (scanner.hasNext()) {

						String token = scanner.next();

						if (token.startsWith("time=")) {
							currentTime = token.substring(5);
						}
					}

					createPercentage(duration, currentTime);

					scanner.close();

				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		int exitCode = 0;
		try {
			exitCode = process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (exitCode == 0) {
			LOGGER.warn("Conversion successful!.");
			setWaveName(newWavName);
		} else {
			LOGGER.warn("Conversion failed with exit code: {}", exitCode);
		}
	}

	/**
	 * Creates the percentage.
	 *
	 * @param duration    the duration
	 * @param currentTime the current time
	 */
	private void createPercentage(String duration, String currentTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SS");
		LocalTime startTime = LocalTime.parse(duration, formatter);
		LocalTime endTime = LocalTime.parse(currentTime, formatter);
		long startSeconds = (long) startTime.toSecondOfDay() + (startTime.getNano() / 1_000_000_000);
		long endSeconds = (long) endTime.toSecondOfDay() + (endTime.getNano() / 1_000_000_000);

		double percent = ((double) endSeconds / startSeconds) * 100;

		DecimalFormat df = new DecimalFormat("#.00");

		String formattedPercent = df.format(percent);

		LOGGER.warn(new StringBuilder().append("/r").append(formattedPercent).append(" completed."));

	}

	/**
	 * Creates the command.
	 *
	 * @param newWavName the new wav name
	 * @return the list
	 */
	private List<String> createCommand(String newWavName) {

		List<String> command = new ArrayList<>();

		command.add(ffmpegLocation);
		command.add("-y");
		command.add("-i");
		command.add("\"" + audioBookLocation + "\"");
		command.add("-c:a");
		command.add("pcm_s16le");
		command.add("-ar");
		command.add("22050");
		command.add("-sample_fmt");
		command.add("s16");
		command.add("-b:a");
		command.add("256k");
		command.add("\"" + newWavName + "\"");

		return command;

	}

	/**
	 * Convert to time format.
	 *
	 * @param bigDecimalSeconds the big decimal seconds
	 * @return the string
	 */
	private String convertToTimeFormat(BigDecimal bigDecimalSeconds) {
		int totalSeconds = bigDecimalSeconds.intValue();
		int hours = totalSeconds / 3600;
		int minutes = (totalSeconds % 3600) / 60;
		int seconds = totalSeconds % 60;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds - 1);
	}

	/**
	 * Gets the file name.
	 *
	 * @return the file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the file name.
	 *
	 * @param fileName the new file name
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Gets the wave name.
	 *
	 * @return the wave name
	 */
	public String getWaveName() {
		return waveName;
	}

	/**
	 * Sets the wave name.
	 *
	 * @param waveName the new wave name
	 */
	public void setWaveName(String waveName) {
		this.waveName = waveName;
	}

}

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

public class AudioTranscriber {

	private static final Logger LOGGER = LogManager.getLogger(AudioTranscriber.class);

	private static final int BUFFER_SIZE = 8192;

	private String ffmpegLocation = "";

	private String audioBookLocation = "";

	private String fileName = "";

	private String waveName = "";

	public AudioTranscriber(String ffmpegLocation, String audioBookLocation) {
		this.ffmpegLocation = ffmpegLocation;
		this.audioBookLocation = audioBookLocation;

		Path path = Paths.get(audioBookLocation);

		fileName = path.getFileName().toString();
	}

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

		float sampleRate = getWavSampleRate();

		System.out.println(sampleRate);
		System.out.println(waveName);

		try (Model model = new Model("model");
				InputStream ais = AudioSystem
						.getAudioInputStream(new BufferedInputStream(new FileInputStream(waveName)));
				Recognizer recognizer = new Recognizer(model, sampleRate)) {

			recognizer.setWords(true);

			int nbytes;
			byte[] b = new byte[BUFFER_SIZE];
			while ((nbytes = ais.read(b)) >= 0) {

				if (recognizer.acceptWaveForm(b, nbytes)) {

					String result = recognizer.getFinalResult();

					JSONObject jsonObject = new JSONObject(result);

					System.out.println(result);

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
	 * 
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
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void convertToWav() {
		String newWavName = fileName.split("\\.")[0] + ".wav";
		String[] command = { ffmpegLocation, "-y", "-i", "\"" + audioBookLocation + "\"", "-c:a", "pcm_s16le", "-ar",
				"22050", "-sample_fmt", "s16", "-b:a", "256k", "\"" + newWavName + "\"" };

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectErrorStream(true);

		Process process = null;
		try {
			process = processBuilder.start();

			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = br.readLine()) != null) {

				System.out.println(line);

				if (line.contains("Duration:")) {
					Scanner sc = new Scanner(line);
					sc.useDelimiter(": ");
					sc.next();
					String duration = sc.next();
					System.out.println("Duration: " + duration);
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

	public static String convertToTimeFormat(BigDecimal bigDecimalSeconds) {
		int totalSeconds = bigDecimalSeconds.intValue();
		int hours = totalSeconds / 3600;
		int minutes = (totalSeconds % 3600) / 60;
		int seconds = totalSeconds % 60;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds - 1);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getWaveName() {
		return waveName;
	}

	public void setWaveName(String waveName) {
		this.waveName = waveName;
	}

}

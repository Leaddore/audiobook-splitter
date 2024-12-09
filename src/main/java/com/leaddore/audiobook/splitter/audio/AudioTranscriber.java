package com.leaddore.audiobook.splitter.audio;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import com.leaddore.audiobook.splitter.range.Range;

public class AudioTranscriber {

	private static final int BUFFER_SIZE = 8192;

	String fileLocation = "";

	public AudioTranscriber(String fileLocation) {
		this.fileLocation = fileLocation;
	}

	public List<Range> transcribe(String string) throws Exception {

		List<String> wordList = new ArrayList<>();
		List<Range> timeCodes = new ArrayList<>();

		wordList.add("most");

		LibVosk.setLogLevel(LogLevel.WARNINGS);

		String[] command = { string, "-y", "-i", "real.mp3", "temp.wav" };

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectErrorStream(true);

		Process process = processBuilder.start();

		int exitCode = process.waitFor();
		if (exitCode == 0) {
			System.out.println("Conversion successful!");
		} else {
			System.out.println("Conversion failed with exit code " + exitCode);
		}

		try (Model model = new Model("model");
				InputStream ais = AudioSystem
						.getAudioInputStream(new BufferedInputStream(new FileInputStream("temp.wav")));
				Recognizer recognizer = new Recognizer(model, 16000)) {

			recognizer.setWords(true);

			int nbytes;
			byte[] b = new byte[BUFFER_SIZE];
			while ((nbytes = ais.read(b)) >= 0) {

				recognizer.acceptWaveForm(b, nbytes);

			}

			String result = recognizer.getFinalResult();

			JSONObject jsonObject = new JSONObject(result);
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
						timeCodes.add(range);
						Range range2 = new Range();
						range2.setStartTime(convertedTime);
						timeCodes.add(range2);

					} else {

						timeCodes.get(timeCodes.size() - 1).setStopTime(convertedTime);
						Range range = new Range();
						range.setStartTime(convertedTime);
						timeCodes.add(range);

					}

					for (Range r : timeCodes) {
						System.out.println(r.getStartTime());
						System.out.println(r.getStopTime());
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

	public static String convertToTimeFormat(BigDecimal bigDecimalSeconds) {
		int totalSeconds = bigDecimalSeconds.intValue();
		int hours = totalSeconds / 3600;
		int minutes = (totalSeconds % 3600) / 60;
		int seconds = totalSeconds % 60;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds - 1);
	}

}

package com.leaddore.audiobook.splitter.splitbook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.leaddore.audiobook.splitter.range.Range;

public class SplitBook {

	private List<Range> range;

	private String audioBookPath = "";

	private String ffmpegPath = "";

	public SplitBook(List<Range> range, String audioBookPath, String ffmpegPath) {

		setRange(range);
		setAudioBookPath(audioBookPath);
		setFfmpegPath(ffmpegPath);

	}

	public List<Range> getRange() {
		return range;
	}

	public void setRange(List<Range> range) {
		this.range = range;
	}

	public void splitItUp() {

		for (int i = 0; i < range.size(); i++) {

			Range timeRange = range.get(i);

			splitMp3(timeRange, i);

		}

	}

	private void splitMp3(Range timeRange, int i) {

		List<String> command = generateCommand(timeRange, i);

		ProcessBuilder pb = new ProcessBuilder(command);

		try {
			Process process = pb.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
			process.waitFor();
			System.out.println("Segment created: " + audioBookPath.split("\\.")[0] + i + ".mp3");

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private List<String> generateCommand(Range timeRange, int i) {

		List<String> command = new ArrayList<>();
		command.add(ffmpegPath);
		command.add("-i");
		command.add(audioBookPath);
		command.add("-ss");
		command.add(timeRange.getStartTime());
		command.add("-to");
		command.add(timeRange.getStopTime());
		command.add("-c");
		command.add("copy");
		command.add(audioBookPath.split("\\.")[0] + i + ".mp3");

		return command;
	}

	public String getAudioBookPath() {
		return audioBookPath;
	}

	public void setAudioBookPath(String audioBookPath) {
		this.audioBookPath = audioBookPath;
	}

	public String getFfmpegPath() {
		return ffmpegPath;
	}

	public void setFfmpegPath(String ffmpegPath) {
		this.ffmpegPath = ffmpegPath;
	}

}

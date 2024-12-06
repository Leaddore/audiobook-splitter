package com.leaddore.audiobook.splitter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.UnsupportedAudioFileException;

public class Main {

	private static final int BUFFER_SIZE = 8192;

	private static String ffmpegPath = "";

	private static final String FFMPEGNAME = "ffmpeg";

	public static void main(String[] args) throws IOException, UnsupportedAudioFileException {

		// LibVosk.setLogLevel(LogLevel.DEBUG);

		String osName = System.getProperty("os.name");

		if (osName.toLowerCase().contains("win")) {
			Map<String, String> envMap = System.getenv();
			for (Map.Entry<String, String> entries : envMap.entrySet()) {

				if ("path".equalsIgnoreCase(entries.getKey())) {

					List<String> envList = Arrays.asList(entries.getValue().split(";"));

					for (String entry : envList) {
						if (entry.contains(getFfmpegname())) {

							setFfmpegPath(entry);

							System.out.println(getFfmpegPath());
							return;

						}
					}

				}
			}
		} else if ((osName.contains("nix") || osName.contains("nux") || osName.contains("aix"))) {

			List<String> pathList = Arrays.asList(System.getenv("PATH").split(":"));

			for (String path : pathList) {

				File file = new File(path, FFMPEGNAME);

				if (Files.isExecutable(file.toPath())) {
					System.out.println("Found " + FFMPEGNAME + " at: " + file.getAbsolutePath());
					return;
				}

			}

		}

	}

	public static String getFfmpegPath() {
		return ffmpegPath;
	}

	public static void setFfmpegPath(String ffmpegPath) {
		Main.ffmpegPath = ffmpegPath;
	}

	public static int getBufferSize() {
		return BUFFER_SIZE;
	}

	public static String getFfmpegname() {
		return FFMPEGNAME;
	}
}
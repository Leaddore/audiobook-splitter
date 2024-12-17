package com.leaddore.audiobook.splitter;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.leaddore.audiobook.splitter.audio.AudioTranscriber;
import com.leaddore.audiobook.splitter.range.Range;

/**
 * The Class Main.
 */
public class Main {

	/** The Constant BUFFER_SIZE. */
	private static final int BUFFER_SIZE = 8192;

	/** The ffmpeg path. */
	private static String ffmpegPath = "";

	private static String audioBookPath = "";

	/** The Constant FFMPEGNAME. */
	private static final String FFMPEGNAME = "ffmpeg";

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(Main.class);

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception
	 */
	public static void main(String... args) throws Exception {

		CommandLine cmd = createCommandLineArgs(args);

		if (cmd.hasOption("h")) {

			LOGGER.warn("Help for Audiobook splitter:");
			for (Option option : cmd.getOptions()) {

				LOGGER.warn("Option {} Desc: {}", option.getOpt(), option.getDescription());

			}
			System.exit(0);

		}

		if (cmd.hasOption("n")) {

			setAudioBookPath(cmd.getOptionValue("n"));

		}

		dealWithFfmpeg(cmd);

		AudioTranscriber transcribe = new AudioTranscriber(getFfmpegPath(), getAudioBookPath());

		List<Range> timeCodes = transcribe.transcribe();

	}

	/**
	 * Deal with ffmpeg.
	 *
	 * @param cmd the cmd
	 */
	private static void dealWithFfmpeg(CommandLine cmd) {
		if (cmd.hasOption("f")) {
			setFfmpegPath(cmd.getOptionValue("f"));
			return;
		}

		String osName = System.getProperty("os.name").toLowerCase();

		if (osName.contains("win")) {
			handleWindowsEnvironment();
		} else if (isUnixLike(osName)) {
			handleUnixEnvironment();
		}
	}

	/**
	 * Handle windows environment.
	 */
	private static void handleWindowsEnvironment() {
		Map<String, String> envMap = System.getenv();
		for (Map.Entry<String, String> entry : envMap.entrySet()) {
			if ("path".equalsIgnoreCase(entry.getKey())) {
				findFfmpegInPath(entry.getValue(), ";");
			}
		}
	}

	/**
	 * Handle unix environment.
	 */
	private static void handleUnixEnvironment() {
		String path = System.getenv("PATH");
		findFfmpegInPath(path, ":");
	}

	/**
	 * Find ffmpeg in path.
	 *
	 * @param path      the path
	 * @param delimiter the delimiter
	 */
	private static void findFfmpegInPath(String path, String delimiter) {
		List<String> pathList = Arrays.asList(path.split(delimiter));
		for (String entry : pathList) {
			if (entry.contains(getFfmpegname()) || (new File(entry, getFfmpegname()).exists()
					&& Files.isExecutable(new File(entry, getFfmpegname()).toPath()))) {

				if (entry.endsWith(".exe") || entry.endsWith(FFMPEGNAME)) {
					setFfmpegPath(entry);
				} else {
					setFfmpegPath(entry + "/" + FFMPEGNAME);
				}

				return;
			}
		}
	}

	/**
	 * Checks if is unix like.
	 *
	 * @param osName the os name
	 * @return true, if is unix like
	 */
	private static boolean isUnixLike(String osName) {
		return osName.contains("nix") || osName.contains("nux") || osName.contains("aix");
	}

	/**
	 * Creates the command line args.
	 *
	 * @param args the args
	 * @return the command line
	 */
	private static CommandLine createCommandLineArgs(String... args) {

		CommandLine cmd = null;

		Options options = new Options();
		options.addOption(new Option("f", "ffmpegFilePath", true, "File path the the ffmpeg installation"));
		options.addOption(new Option("n", "audioFileName", true,
				"Name of the Audiofile to break into chapters with path if not located in the same directory."));
		options.addOption(new Option("h", "help", false, "Show Help (this list)"));

		CommandLineParser parser = new DefaultParser();

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return cmd;

	}

	/**
	 * Gets the ffmpeg path.
	 *
	 * @return the ffmpeg path
	 */
	public static String getFfmpegPath() {
		return ffmpegPath;
	}

	/**
	 * Sets the ffmpeg path.
	 *
	 * @param ffmpegPath the new ffmpeg path
	 */
	public static void setFfmpegPath(String ffmpegPath) {
		Main.ffmpegPath = ffmpegPath;
	}

	/**
	 * Gets the buffer size.
	 *
	 * @return the buffer size
	 */
	public static int getBufferSize() {
		return BUFFER_SIZE;
	}

	/**
	 * Gets the ffmpegname.
	 *
	 * @return the ffmpegname
	 */
	public static String getFfmpegname() {
		return FFMPEGNAME;
	}

	public static String getAudioBookPath() {
		return audioBookPath;
	}

	public static void setAudioBookPath(String audioBookPath) {
		Main.audioBookPath = audioBookPath;
	}
}
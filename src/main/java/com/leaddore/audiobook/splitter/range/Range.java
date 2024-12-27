package com.leaddore.audiobook.splitter.range;

/**
 * The Class Range.
 */
public class Range {

	/** The start time. */
	private String startTime;

	/** The stop time. */
	private String stopTime;

	/** The title. */
	private String title;

	/**
	 * Gets the start time.
	 *
	 * @return the start time
	 */
	public String getStartTime() {
		return startTime;
	}

	/**
	 * Sets the start time.
	 *
	 * @param startTime the new start time
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	/**
	 * Gets the stop time.
	 *
	 * @return the stop time
	 */
	public String getStopTime() {
		return stopTime;
	}

	/**
	 * Sets the stop time.
	 *
	 * @param stopTime the new stop time
	 */
	public void setStopTime(String stopTime) {
		this.stopTime = stopTime;
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();

		builder.append(getStartTime()).append(" - ").append(getStopTime()).append(" - ").append(getTitle());

		return builder.toString();
	}

	/**
	 * Gets the title.
	 *
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title.
	 *
	 * @param title the new title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

}

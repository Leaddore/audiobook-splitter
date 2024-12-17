package com.leaddore.audiobook.splitter.range;

public class Range {

	private String startTime;

	private String stopTime;

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getStopTime() {
		return stopTime;
	}

	public void setStopTime(String stopTime) {
		this.stopTime = stopTime;
	}

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();

		builder.append(getStartTime()).append(" - ").append(getStopTime());

		return builder.toString();
	}

}

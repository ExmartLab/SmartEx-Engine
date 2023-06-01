package exengine.datamodel;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class LogEntry implements Comparable<LogEntry> {

	private String time;
	private String name;
	private String state;
	private String entityId;
	private ArrayList<String> other;

	public LogEntry() {
	}

	public LogEntry(String time, String name, String state, String entityId, ArrayList<String> other) {
		this.time = time;
		this.name = name;

		if (state == null) {
			this.state = "null";
		} else {
			this.state = state;
		}

		this.entityId = entityId;
		this.other = other;
	}

	public String toString() {
		String otherString = other == null ? "[]" : other.toString();
		return "time: " + time + " name: " + name + " state: " + state + " entity_id: " + entityId + " other: "
				+ otherString;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		if (state == null) {
			this.state = "null";
		} else {
			this.state = state;
		}
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public ArrayList<String> getOther() {
		return other;
	}

	public void setOther(ArrayList<String> other) {
		this.other = other;
	}
	
	public LocalDateTime getLocalDateTime() {
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		OffsetDateTime offsetDate = OffsetDateTime.parse(this.time, formatter);
		
		return offsetDate.toLocalDateTime();
	}

	@Override
	public int compareTo(LogEntry o) {
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

		OffsetDateTime thisDate = OffsetDateTime.parse(this.time, formatter);
		OffsetDateTime otherDate = OffsetDateTime.parse(o.getTime(), formatter);

		return thisDate.compareTo(otherDate);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		LogEntry otherLogEntry = (LogEntry) o;

		return (this.entityId.equals(otherLogEntry.getEntityId()) && this.state.equals(otherLogEntry.getState()));
	}

}

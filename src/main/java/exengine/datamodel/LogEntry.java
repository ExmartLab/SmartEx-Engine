package exengine.datamodel;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class LogEntry implements Comparable<LogEntry> {

	private String time;
	private String name;
	private String state;
	private String entityId;
	private ArrayList<String> other;
	
	public LogEntry() {};
	
	public LogEntry(String time, String name, String state, String entityId, ArrayList<String> other) {
		this.time = time;
		this.name = name;
		this.state = state;
		this.entityId = entityId;
		this.other = other;
	}
	
	public String toString() {
		String otherString = other == null ? "[]" : other.toString();
		return "time: " + time + " name: " + name + " state: " + state + " entity_id: " + entityId + " other: " + otherString;  
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
		this.state = state;
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

	@Override
	public int compareTo(LogEntry o) {
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		
		OffsetDateTime thisDate = OffsetDateTime.parse(this.time, formatter);
		OffsetDateTime otherDate = OffsetDateTime.parse(o.getTime(), formatter);
		
		return thisDate.compareTo(otherDate);
	}
	
}

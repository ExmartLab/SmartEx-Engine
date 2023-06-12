package exengine.datamodel;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Represents a log entry of the Home Assistant application. This class is used
 * to encapsulate the information of a single log entry of Home Assistant as a
 * Java object. It also simplifies the find cause algorithm by being sortable
 * and comparable.
 */
public class LogEntry implements Comparable<LogEntry> {

	private String time;
	private String name;
	private String state;
	private String entityId;
	private ArrayList<String> other;

	/**
	 * Constructs a new LogEntry object with the specified parameters.
	 * 
	 * @Note Two LogEntry objects are equal if their entityId and state matches.
	 * @Note LogEntry objects are ordered according to the time at which they where
	 *       produced by Home Assistant ("time").
	 * 
	 * @param time     The time stamp of the log entry
	 * @param name     The name associated with the log entry
	 * @param state    The state associated with the log entry
	 * @param entityId The entity ID associated with the log entry
	 * @param other    An ArrayList of additional and optional information
	 */
	public LogEntry(String time, String name, String state, String entityId, ArrayList<String> other) {
		setTime(time);
		setName(name);
		setState(state);
		setEntityId(entityId);
		setOther(other);
	}

	/**
	 * Returns the time stamp of the log entry.
	 *
	 * @return The time stamp of the log entry.
	 */
	public String getTime() {
		return time;
	}

	/**
	 * Sets the time stamp of the log entry.
	 *
	 * @param time The time stamp of the log entry.
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * Returns the name associated with the log entry.
	 *
	 * @return The name associated with the log entry.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name associated with the log entry.
	 *
	 * @param name The name associated with the log entry. If null, the state
	 *             (String) will be set to the String "null", not the null object.
	 */
	public void setName(String name) {
		if (name == null) {
			this.name = "null";
		} else {
			this.name = name;
		}
	}

	/**
	 * Returns the state of the log entry.
	 *
	 * @return The state of the log entry.
	 */
	public String getState() {
		return state;
	}

	/**
	 * Sets the state of the log entry.
	 *
	 * @param state The state of the log entry. If null, the state (String) will be
	 *              set to the String "null", not the null object.
	 */
	public void setState(String state) {
		if (state == null) {
			this.state = "null";
		} else {
			this.state = state;
		}
	}

	/**
	 * Returns the entity ID associated with the log entry.
	 *
	 * @return The entity ID associated with the log entry.
	 */
	public String getEntityId() {
		return entityId;
	}

	/**
	 * Sets the entity ID associated with the log entry.
	 *
	 * @param entityId The entity ID associated with the log entry.
	 */
	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	/**
	 * Returns the additional information related to the log entry.
	 *
	 * @return An ArrayList of additional information related to the log entry.
	 */
	public ArrayList<String> getOther() {
		return other;
	}

	/**
	 * Sets the additional information related to the log entry.
	 *
	 * @param other An ArrayList of additional information related to the log entry.
	 */
	public void setOther(ArrayList<String> other) {
		this.other = other;
	}

	/**
	 * Returns the time in a LocalDateTime object.
	 * 
	 * @return the time as LocalDateTime
	 */
	public LocalDateTime getLocalDateTime() {
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		OffsetDateTime offsetDate = OffsetDateTime.parse(this.time, formatter);

		return offsetDate.toLocalDateTime();
	}

	/**
	 * Compared based on time.
	 */
	@Override
	public int compareTo(LogEntry o) {
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

		OffsetDateTime thisDate = OffsetDateTime.parse(this.time, formatter);
		OffsetDateTime otherDate = OffsetDateTime.parse(o.getTime(), formatter);

		return thisDate.compareTo(otherDate);
	}

	/**
	 * Two LogEntry objects are equal if at least their entityId and state matches.
	 */
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
	
	/**
	 * Note: Needed to override
	 */
	@Override
	public int hashCode() {
	    int result = entityId.hashCode();
	    result = 31 * result + state.hashCode();
	    return result;
	}

}

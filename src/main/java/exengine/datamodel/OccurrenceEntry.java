package exengine.datamodel;

/**
 * The OccurrenceEntry class represents an entry of occurrence information. This
 * is to store how often a user requested an explanation for the same thing
 * (rule or error) in order to taylor the explanation accordingly. It contains
 * the user ID, cause ID, and the time of the occurrence.
 */
public class OccurrenceEntry {

	private String userId;
	private String causeId;
	private long time;

	/**
	 * Constructs an OccurrenceEntry with the specified parameters.
	 *
	 * @param userId  the user ID associated with the occurrence
	 * @param causeId the cause ID associated with the occurrence
	 * @param time    the time of the occurrence
	 */
	public OccurrenceEntry(String userId, String causeId, long time) {
		setUserId(userId);
		setCauseId(causeId);
		setTime(time);
	}

	/**
	 * Returns the user ID associated with the occurrence information.
	 *
	 * @return the user ID associated with the occurrence information
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Sets the user ID associated with the occurrence information.
	 *
	 * @param userId the user ID associated with the occurrence information
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * Returns the cause ID associated with the occurrence information.
	 *
	 * @return the cause ID associated with the occurrence information
	 */
	public String getCauseId() {
		return causeId;
	}

	/**
	 * Sets the cause ID associated with the occurrence information.
	 *
	 * @param causeId the cause ID associated with the occurrence information
	 */
	public void setCauseId(String causeId) {
		this.causeId = causeId;
	}

	/**
	 * Returns the time of the occurrence information.
	 *
	 * @return the time of the occurrence information
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Sets the time of the occurrence information.
	 *
	 * @param time the time of the occurrence information
	 */
	public void setTime(long time) {
		this.time = time;
	}
}

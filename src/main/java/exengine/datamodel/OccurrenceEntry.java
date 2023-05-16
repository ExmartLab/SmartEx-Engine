package exengine.datamodel;

public class OccurrenceEntry {

	private String userId;
	private String causeId;
	private long time;

	public OccurrenceEntry() {
	}

	public OccurrenceEntry(String userId, String causeId, long time) {
		setUserId(userId);
		setCauseId(causeId);
		setTime(time);
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCauseId() {
		return causeId;
	}

	public void setCauseId(String causeId) {
		this.causeId = causeId;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
}

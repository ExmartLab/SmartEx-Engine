package exengine.datamodel;

public class OccurrenceEntry {

	private String userId;
	private String ruleId;
	private long time;
	
	public OccurrenceEntry() {
	}
	
	public OccurrenceEntry(String userId, String ruleId, long time) {
		setUserId(userId);
		setRuleId(ruleId);
		setTime(time);
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
}

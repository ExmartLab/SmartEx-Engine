package exengine.datamodel;

import java.sql.Timestamp;

public class OccurrenceEntry {

	private String userId;
	private String ruleId;
	private Timestamp time;
	
	public OccurrenceEntry() {
	}
	
	public OccurrenceEntry(String userId, String ruleId, Timestamp time) {
		this.setUserId(userId);
		this.setRuleId(ruleId);
		this.setTime(time);
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

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}
}

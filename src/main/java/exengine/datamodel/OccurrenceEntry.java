package exengine.datamodel;

import java.sql.Timestamp;

public class OccurrenceEntry {

	private int userId;
	private int ruleId;
	private Timestamp time;
	
	public OccurrenceEntry() {
	}
	
	public OccurrenceEntry(int userId, int ruleId, Timestamp time) {
		this.setUserId(userId);
		this.setRuleId(ruleId);
		this.setTime(time);
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getRuleId() {
		return ruleId;
	}

	public void setRuleId(int ruleId) {
		this.ruleId = ruleId;
	}

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}
}

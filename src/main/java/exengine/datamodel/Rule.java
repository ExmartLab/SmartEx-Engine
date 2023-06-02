package exengine.datamodel;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;

public class Rule {

	@Id
	private String id; // only for MongoDB
	private String ruleName;
	private LogEntry ruleEntry;
	private ArrayList<LogEntry> trigger;
	private ArrayList<String> conditions;
	private ArrayList<LogEntry> actions;
	private String ownerId;
	private String ruleDescription;
	private String ruleId;

	public Rule() {
	}

	public Rule(String ruleName, String ruleId, LogEntry ruleEntry, ArrayList<LogEntry> trigger,
			ArrayList<String> conditions, ArrayList<LogEntry> actions, String ownerId, String ruleDescription) {
		setRuleName(ruleName);
		setRuleId(ruleId);
		setRuleEntry(ruleEntry);
		setTrigger(trigger);
		setConditions(conditions);
		setActions(actions);
		setOwnerId(ownerId);
		setRuleDescription(ruleDescription);
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public ArrayList<LogEntry> getTrigger() {
		if (trigger != null)
			return trigger;
		return new ArrayList<>();
	}

	public void setTrigger(ArrayList<LogEntry> trigger) {
		this.trigger = trigger;
	}

	public ArrayList<LogEntry> getActions() {
		return actions;
	}

	public void setActions(ArrayList<LogEntry> actions) {
		this.actions = actions;
	}

	public ArrayList<String> getConditions() {
		return conditions;
	}

	public void setConditions(ArrayList<String> conditions) {
		this.conditions = conditions;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getRuleDescription() {
		return ruleDescription;
	}

	public void setRuleDescription(String ruleDescription) {
		this.ruleDescription = ruleDescription;
	}

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public LogEntry getRuleEntry() {
		return ruleEntry;
	}

	public void setRuleEntry(LogEntry ruleEntry) {
		this.ruleEntry = ruleEntry;
	}
}

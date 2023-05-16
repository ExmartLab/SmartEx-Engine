package exengine.datamodel;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;

public class Rule {

	@Id
	private String id;
	private String ruleName;
	private LogEntry ruleEntry;
	private ArrayList<LogEntry> trigger;
	private ArrayList<String> conditions;
	private ArrayList<LogEntry> actions;

	private String triggerString;
	private String conditionsString;
	private String actionsString;
	private String ownerId;
	private String ruleDescription;
	private String ruleId;

	public Rule() {
	}

	public Rule(String ruleName, String ruleId, LogEntry ruleEntry, ArrayList<LogEntry> trigger,
			ArrayList<String> conditions, ArrayList<LogEntry> actions, String ownerId, String ruleDescription) {
		this.ruleName = ruleName;
		this.ruleId = ruleId;
		this.ruleEntry = ruleEntry;
		this.trigger = trigger;
		this.conditions = conditions;
		this.actions = actions;
		this.ownerId = ownerId;
		this.ruleDescription = ruleDescription;

		triggerString = trigger == null ? "[]" : trigger.toString();
		conditionsString = conditions == null ? "[]" : conditions.toString();
		actionsString = actions == null ? "[]" : actions.toString();
	}

	@Override
	public String toString() {
		return String.format("Path[id=%s, name='%s', trigger='%s', action='%s', owner='%s']", id, ruleName,
				triggerString, conditionsString, actionsString, ownerId);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
		triggerString = trigger == null ? "[]" : trigger.toString();
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
		conditionsString = conditions == null ? "[]" : conditions.toString();
	}

	public String getTriggerString() {
		return triggerString;
	}

	public void setTriggerString(String triggerString) {
		this.triggerString = triggerString;
	}

	public String getConditionsString() {
		return conditionsString;
	}

	public void setConditionsString(String conditionsString) {
		this.conditionsString = conditionsString;
	}

	public String getActionsString() {
		return actionsString;
	}

	public void setActionsString(String actionsString) {
		this.actionsString = actionsString;
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

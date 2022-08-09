package exengine.datamodel;

import java.util.List;

import org.springframework.data.annotation.Id;

public class Rule {

	@Id
	public String id;

	public String ruleName;
	public List<String> trigger;
	public List<String> conditions;
	public List<String> actions;

	String triggerString;
	String conditionsString;
	String actionsString;

	String ownerId;

	public Rule() {
	}

	public Rule(String ruleName, List<String> trigger, List<String> conditions, List<String> actions, String ownerId) {
		this.ruleName = ruleName;
		this.trigger = trigger;
		this.conditions = conditions;
		this.actions = actions;
		this.ownerId = ownerId;

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

	public List<String> getTrigger() {
		return trigger;
	}

	public void setTrigger(List<String> trigger) {
		this.trigger = trigger;
	}

	public List<String> getActions() {
		return actions;
	}

	public void setActions(List<String> actions) {
		this.actions = actions;
	}

	public List<String> getConditions() {
		return conditions;
	}

	public void setConditions(List<String> conditions) {
		this.conditions = conditions;
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
}

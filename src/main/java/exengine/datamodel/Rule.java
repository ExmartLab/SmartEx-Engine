package exengine.datamodel;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;

public class Rule {

	@Id
	public String id;

	public String ruleName;
	public ArrayList<String> trigger;
	public ArrayList<String> conditions;
	public ArrayList<String> actions;

	String triggerString;
	String conditionsString;
	String actionsString;

	String ownerId;

	public Rule() {
	}

	public Rule(String ruleName, ArrayList<String> trigger, ArrayList<String> conditions, ArrayList<String> actions, String ownerId) {
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

	public ArrayList<String> getTrigger() {
		return trigger;
	}

	public void setTrigger(ArrayList<String> trigger) {
		this.trigger = trigger;
		triggerString = trigger == null ? "[]" : trigger.toString();
	}

	public ArrayList<String> getActions() {
		return actions;
	}

	public void setActions(ArrayList<String> actions) {
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
		actionsString = actions == null ? "[]" : actions.toString();
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
}

package exengine.datamodel;

import java.util.ArrayList;

public class Cause {

	private Rule rule;
	
	private LogEntry trigger;
	private ArrayList<String> conditions;
	private ArrayList<LogEntry> actions;

	private String conditionsString;
	private String actionsString;
	private String triggerString;
	
	public Cause(LogEntry trigger, ArrayList<String> conditions, ArrayList<LogEntry> actions, Rule rule) {
		this.trigger = trigger;
		setTriggerString();
		this.conditions = conditions;
		setConditionsString();
		this.actions = actions;
		setActionsString();
		this.rule = rule;
	}

	public LogEntry getTrigger() {
		return trigger;
	}

	public void setTrigger(LogEntry trigger) {
		this.trigger = trigger;
	}

	public ArrayList<String> getConditions() {
		return conditions;
	}

	public void setConditions(ArrayList<String> conditions) {
		this.conditions = conditions;
		setConditionsString();
	}

	public ArrayList<LogEntry> getActions() {
		return actions;
	}

	public void setActions(ArrayList<LogEntry> actions) {
		this.actions = actions;
		setActionsString();
	}

	public Rule getRule() {
		return rule;
	}

	public void setRule(Rule rule) {
		this.rule = rule;
	}
	
	public void setConditionsString() {
		conditionsString = "[";
		for(String c : conditions) {
			conditionsString = getConditionsString() + c +";";
		}
		conditionsString = getConditionsString() + "]";
	}
	
	public void setActionsString() {
		actionsString = "[";
		for(LogEntry a : actions) {
			actionsString = getActionsString() + a.name + "|" + a.state +";";
		}
		actionsString = getActionsString() + "]";
	}

	public String getConditionsString() {
		return conditionsString;
	}

	public String getActionsString() {
		return actionsString;
	}
	
	public void setTriggerString() {
		triggerString = trigger == null ? "null" : trigger.name + " " + trigger.state;
	}
	
	public String getTriggerString() {
		return triggerString;
	}
}

package exengine.datamodel;

import java.util.ArrayList;

public class Cause {

	private Rule rule;
	
	private LogEntry trigger;
	private ArrayList<String> conditions;
	private ArrayList<LogEntry> actions;
	
	public Cause(LogEntry trigger, ArrayList<String> conditions, ArrayList<LogEntry> actions, Rule rule) {
		this.trigger = trigger;
		this.conditions = conditions;
		this.actions = actions;
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
	}

	public ArrayList<LogEntry> getActions() {
		return actions;
	}

	public void setActions(ArrayList<LogEntry> actions) {
		this.actions = actions;
	}

	public Rule getRule() {
		return rule;
	}

	public void setRule(Rule rule) {
		this.rule = rule;
	}	
	
}

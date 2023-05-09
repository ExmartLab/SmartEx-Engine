package exengine.datamodel;

import java.util.ArrayList;

public class RuleCause extends Cause {

	private Rule rule;
	
	private LogEntry trigger;
	private ArrayList<String> conditions;

	private String conditionsString;
	private String triggerString;
	
	public RuleCause(LogEntry trigger, ArrayList<String> conditions, ArrayList<LogEntry> actions, Rule rule) {
		super(actions);
		this.trigger = trigger;
		setTriggerString();
		this.conditions = conditions;
		setConditionsString();
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

	public String getConditionsString() {
		return conditionsString;
	}
	
	public void setTriggerString() {
		triggerString = trigger == null ? "null" : trigger.getName() + " " + trigger.getState();
	}
	
	public String getTriggerString() {
		return triggerString;
	}
}

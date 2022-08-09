package exengine.explanationgenerationservice;

import java.util.ArrayList;

public class Cause {

	public String trigger;
	public ArrayList<String> conditions;
	public ArrayList<String> actions;
	public String rule;
	
	public Cause(String trigger, ArrayList<String> conditions, ArrayList<String> actions, String rule) {
		this.trigger = trigger;
		this.conditions = conditions;
		this.actions = actions;
		this.rule = rule;
	}

	public String getTrigger() {
		return trigger;
	}

	public void setTrigger(String trigger) {
		this.trigger = trigger;
	}

	public ArrayList<String> getConditions() {
		return conditions;
	}

	public void setConditions(ArrayList<String> conditions) {
		this.conditions = conditions;
	}

	public ArrayList<String> getActions() {
		return actions;
	}

	public void setActions(ArrayList<String> actions) {
		this.actions = actions;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}
	
	
}

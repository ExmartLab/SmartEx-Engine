package exengine.database;

import java.util.List;

import org.springframework.data.annotation.Id;

public class Rule {

	@Id
	public String id;

	public String ruleName;
	public List<String> trigger;
	public List<String> actions;

	public Rule() {
	}

	public Rule(String ruleName, List<String> trigger, List<String> actions) {
		this.ruleName = ruleName;
		this.trigger = trigger;
		this.actions = actions;
	}

	@Override
	public String toString() {
		String triggerString = trigger == null ? "[]" : trigger.toString();
		String actionsString = actions == null ? "[]" : actions.toString();
		return String.format("Path[id=%s, name='%s', trigger='%s', action='%s']", id, ruleName, triggerString, actionsString);
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
}

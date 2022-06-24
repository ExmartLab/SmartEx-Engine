package exengine.database;

import java.util.List;

import org.springframework.data.annotation.Id;

public class Rule {

	@Id
	public String id;

	public String ruleName;
	public List<String> trigger;
	public String action;

	public Rule() {
	}

	public Rule(String ruleName, List<String> trigger, String action) {
		this.ruleName = ruleName;
		this.trigger = trigger;
		this.action = action;
	}

	@Override
	public String toString() {
		return String.format("Path[id=%s, name='%s', trigger='%s', action='%s']", id, ruleName, trigger.toString(), action);
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

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}

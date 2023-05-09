package exengine.datamodel;

import java.util.ArrayList;

public abstract class Cause {

	private ArrayList<LogEntry> actions;
	private String actionsString;

	public Cause(ArrayList<LogEntry> actions) {
		setActions(actions);
	}

	public ArrayList<LogEntry> getActions() {
		return actions;
	}

	public void setActions(ArrayList<LogEntry> actions) {
		this.actions = actions;
		setActionsString();
	}

	public void setActionsString() {
		actionsString = "[";
		for (LogEntry a : actions) {
			actionsString = getActionsString() + a.getName() + "|" + a.getState() + ";";
		}
		actionsString = getActionsString() + "]";
	}

	public String getActionsString() {
		return actionsString;
	}

}

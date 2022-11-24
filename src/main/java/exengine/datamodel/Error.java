package exengine.datamodel;

import java.util.ArrayList;

public class Error {


	public ArrayList<LogEntry> actions;
	public String implication;
	public String solution;
	
	public String errorName; 
	public String errorId;
	

	public Error(String errorName, String errorId, ArrayList<LogEntry> actions, String implication, String solution) {
		this.errorName = errorName;
		this.errorId = errorId;
		this.actions = actions;
		this.implication = implication;
		this.solution = solution;
	}
	
	public ArrayList<LogEntry> getActions() {
		return actions;
	}

	public void setActions(ArrayList<LogEntry> actions) {
		this.actions = actions;
	}

	
}

package exengine.datamodel;

import java.util.ArrayList;

public class Error {

	private ArrayList<LogEntry> actions;
	private String implication;
	private String solution;
	private String errorName;
	private String errorId;

	public Error() {
	}

	public Error(String errorName, String errorId, ArrayList<LogEntry> actions, String implication, String solution) {
		setErrorName(errorName);
		setErrorId(errorId);
		setActions(actions);
		setImplication(implication);
		setSolution(solution);
	}

	public ArrayList<LogEntry> getActions() {
		return actions;
	}

	public void setActions(ArrayList<LogEntry> actions) {
		this.actions = actions;
	}

	public String getErrorId() {
		return errorId;
	}

	public void setErrorId(String errorId) {
		this.errorId = errorId;
	}

	public String getSolution() {
		return solution;
	}

	public void setSolution(String solution) {
		this.solution = solution;
	}

	public String getImplication() {
		return implication;
	}

	public void setImplication(String implication) {
		this.implication = implication;
	}

	public void setErrorName(String errorName) {
		this.errorName = errorName;
	}

	public String getErrorName() {
		return errorName;
	}

}

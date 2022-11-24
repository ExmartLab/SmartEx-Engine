package exengine.datamodel;

import java.util.ArrayList;

public class ErrorCause extends Cause {

	private String implication;
	private String solution;
	
	public ErrorCause(ArrayList<LogEntry> actions, String implication, String solution) {
		super(actions);
		setImplication(implication);
		setSolution(solution);
	}

	public String getImplication() {
		return implication;
	}

	public void setImplication(String implication) {
		this.implication = implication;
	}

	public String getSolution() {
		return solution;
	}

	public void setSolution(String solution) {
		this.solution = solution;
	}
	
}

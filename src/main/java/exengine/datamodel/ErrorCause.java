package exengine.datamodel;

import java.util.ArrayList;

public class ErrorCause extends Cause {

	private String implication;
	private String solution;
	private Error error;
	
	public ErrorCause(ArrayList<LogEntry> actions, String implication, String solution, Error error) {
		super(actions);
		setImplication(implication);
		setSolution(solution);
		setError(error);
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
	
	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}
	
}

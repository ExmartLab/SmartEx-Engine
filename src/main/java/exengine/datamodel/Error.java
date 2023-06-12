package exengine.datamodel;

import java.util.ArrayList;

/**
 * The Error class represents an error in the system. It contains information
 * about the error name, error ID, associated actions, implication, and
 * solution.
 */
public class Error {

	private ArrayList<LogEntry> actions;
	private String implication;
	private String solution;
	private String errorName;
	private String errorId;

	/**
	 * Constructs a new Error object with the specified parameters.
	 *
	 * @param errorName   the name of the error
	 * @param errorId     the ID of the error
	 * @param actions     the list of actions associated with the error (i.e.,
	 *                    things that are logged that indicate this error occurred)
	 * @param implication the implication of the error
	 * @param solution    the solution to the error
	 */
	public Error(String errorName, String errorId, ArrayList<LogEntry> actions, String implication, String solution) {
		setErrorName(errorName);
		setErrorId(errorId);
		setActions(actions);
		setImplication(implication);
		setSolution(solution);
	}

	/**
	 * Returns the list of actions associated with the error.
	 *
	 * @return the list of actions associated with the error
	 */
	public ArrayList<LogEntry> getActions() {
		return actions;
	}

	/**
	 * Returns the list of actions associated with the error.
	 *
	 * @return the list of actions associated with the error
	 */
	public void setActions(ArrayList<LogEntry> actions) {
		this.actions = actions;
	}

	/**
	 * Returns the ID of the error.
	 *
	 * @return the ID of the error
	 */
	public String getErrorId() {
		return errorId;
	}

	/**
	 * Sets the ID of the error.
	 *
	 * @param errorId the ID of the error
	 */
	public void setErrorId(String errorId) {
		this.errorId = errorId;
	}

	/**
	 * Returns the solution to the error.
	 *
	 * @return the solution to the error
	 */
	public String getSolution() {
		return solution;
	}

	/**
	 * Sets the solution to the error.
	 *
	 * @param solution the solution to the error
	 */
	public void setSolution(String solution) {
		this.solution = solution;
	}

	/**
	 * Returns the implication of the error.
	 *
	 * @return the implication of the error
	 */
	public String getImplication() {
		return implication;
	}

	/**
	 * Sets the implication of the error.
	 *
	 * @param implication the implication of the error
	 */
	public void setImplication(String implication) {
		this.implication = implication;
	}

	/**
	 * Sets the name of the error.
	 *
	 * @param errorName the name of the error
	 */
	public void setErrorName(String errorName) {
		this.errorName = errorName;
	}

	/**
	 * Returns the name of the error.
	 *
	 * @return the name of the error
	 */
	public String getErrorName() {
		return errorName;
	}

}

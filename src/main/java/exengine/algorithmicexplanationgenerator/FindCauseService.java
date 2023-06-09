package exengine.algorithmicexplanationgenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;

import org.springframework.stereotype.Service;

import exengine.datamodel.LogEntry;
import exengine.datamodel.Rule;
import exengine.datamodel.Error;

/**
 * Component bundling the functionalities required to determine causes.
 */
@Service
public class FindCauseService {

	/**
	 * Determines the causal path (i.e., the rule or error) that led to the
	 * occurrence of an explanandum.
	 * 
	 * @See Algorithm 1, "Find the Cause Path" in the paper "SmartEx: A Framework
	 *      for Generating User-Centric Explanations in Smart Environments"
	 * 
	 * @Note There are three aspects in which this implementation deviates from the
	 *       algorithm description in the paper (see reference above), which are:
	 * 
	 *       (1.) The rules and log entries are naturally disjoint in this
	 *       application, so no need to extract them from the explanation constructs
	 *       as in the paper (Line 6 and 7 of the algorithm description).
	 * 
	 *       (2.) This method also finds errors (the differences appear only at the
	 *       end, after every part of Algorithm 1 has already happened), the
	 *       so-called Error-Handling Plugin.
	 * 
	 *       (3.) Some naming, precisely: X = logEntries (after line 7 of the
	 *       algorithm was executed), R = dbRules.
	 * 
	 * @Note You find inline comments in the method that reference the lines of the
	 *       description of Algorithm 1.
	 * 
	 * @param explanandum the action that is to be explained
	 * @param logEntries  the list of Home Assistant logs
	 * @param dbRules     the set of rules that exist in system
	 * @param dbErrors    the set of errors that are known about the system
	 * @return the exact rule or error that lead to the occurrence of the specified
	 *         explanandum
	 */
	public Object findCause(LogEntry explanandum, ArrayList<LogEntry> logEntries, List<Rule> dbRules,
			List<Error> dbErrors) {

		// Find Cause Path Algorithm (Algorithm 1 in the Paper):

		Rule firedRule = null; // Line 5

		Collections.sort(logEntries, Collections.reverseOrder()); // Line 8

		ArrayList<Rule> candidateRules = findCandidateRules(explanandum, dbRules); // Line 9

		for (Rule rule : candidateRules) { // Line 10
			if (actionsApply(explanandum, rule, logEntries, 1000) // Line 11
					&& preconditionsApply(explanandum, rule, logEntries)) { // Line 12 - 13
				firedRule = rule;
			}
		}

		if (firedRule != null) { // Line 14
			return firedRule;
		}

		// Error-Handling Plug In:

		Error error = getError(explanandum, dbErrors);

		if (error != null) {
			return error;
		}

		// No Path Found:
		return null;

	}

	/**
	 * Retrieves a list of all rules ("candidate rules") whose set of actions
	 * contain the explanandum. This is analoguous to line 9 of the pseudo-code in
	 * the paper.
	 * 
	 * @param explanandum the action that is to be explained
	 * @param dbRules     the set of rules that exist in system
	 * @return list of candidate rules
	 */
	public ArrayList<Rule> findCandidateRules(LogEntry explanandum, List<Rule> dbRules) {
		ArrayList<Rule> candidateRules = new ArrayList<>();

		for (Rule rule : dbRules) {
			if (rule.getActions().contains(explanandum)) {
				candidateRules.add(rule);
			}
		}

		return candidateRules;
	}

	/**
	 * Checks if all the actions of a rule have been fired simultaneously to the
	 * explanandum.
	 * 
	 * @Note Since actions are never fired exactly simulaneously in Home Assistant,
	 *       this function should be called with a tolerance > 0.
	 * 
	 * @param explanandum the action that is to be explained
	 * @param rule        the rule whose actions this check is performed on
	 * @param logEntries  the list of Home Assistant logs
	 * @param tolerance   the maximum number of seconds deviation at which fired
	 *                    actions can be considered fired simultaneously
	 * @return "true", if all the actions of the specified rule have been fired
	 *         simultaneously to the explanandum, given the tolerance, else it will
	 *         return "false"
	 */
	public boolean actionsApply(LogEntry explanandum, Rule rule, ArrayList<LogEntry> logEntries, int tolerance) {
		LocalDateTime explanandumTime = explanandum.getLocalDateTime();
		LocalDateTime startTime = explanandumTime.minusSeconds(tolerance);
		LocalDateTime endTime = explanandumTime.plusSeconds(tolerance);
		ArrayList<LogEntry> filteredLogEntries = filterLogEntriesByTime(logEntries, startTime, endTime);

		return filteredLogEntries.containsAll(rule.getActions());
	}

	/**
	 * Checks the preconditions of a rule have been met prior to the firing of the
	 * explanandum. A precondition is regarded as met if and only if the
	 * <b>latest</b> occurrence of a specific entityId in the list of logs has the
	 * same state as the precondition. This is to avoid that a precondition was met
	 * once upon some time but is not met any more.
	 * 
	 * Further, this simple preconditions check assumes that all preconditions are
	 * simply joint via OR. This corresponds precisely to the "Triggers" to Home
	 * Assistant automations. Therefore this function will return true if any of the
	 * preconditions have been met.
	 * 
	 * @param explanandum the action that is to be explained
	 * @param rule        the rule whose preconditions this check is performed on
	 * @param logEntries  the list of Home Assistant logs
	 * @return "true" if any of the preconditions have been met at the moment the
	 *         explanandum was fired, else it will return "false"
	 * 
	 * @Note Home Assistant also allows for "Conditions", which are preconditions
	 *       joint by AND. For simplicity, this simple preconditions check ignores
	 *       this.
	 */
	public boolean preconditionsApply(LogEntry explanandum, Rule rule, ArrayList<LogEntry> logEntries) {

		// 1. Step: Filter out log entries that occurred after the explanandum
		LocalDateTime startTime = LocalDateTime.MIN;
		LocalDateTime endTime = explanandum.getLocalDateTime(); // does this include the upper boundary? -> Yes
		ArrayList<LogEntry> filteredLogEntries = filterLogEntriesByTime(logEntries, startTime, endTime);

		// 2. Step: Filter out log entries whose states have been overwritten
		HashSet<String> uniqueEntityIds = new HashSet<>();
		Iterator<LogEntry> iterator = filteredLogEntries.iterator();
		while (iterator.hasNext()) {
			LogEntry logEntry = iterator.next();
			if (!uniqueEntityIds.add(logEntry.getName())) {
				iterator.remove();
			}
		}

		// 3. Step: Check if at least one precondition was satisfied
		for (LogEntry trigger : rule.getTrigger()) {
			if (filteredLogEntries.contains(trigger)) {
				return true;
			}
		}

		// 4. Step: Return false because none of the preconditions was satisfied
		return false;

	}

	/**
	 * Checks if the explanandum is in a list of known errors and in the positive
	 * case, returns the error.
	 * 
	 * @param explanandum the action that is to be explained
	 * @param dbErrors    the set of errors that are known about the system
	 * @return if existing, the error, else will return null
	 */
	public Error getError(LogEntry explanandum, List<Error> dbErrors) {

		for (Error error : dbErrors) {
			ArrayList<LogEntry> actions = error.getActions();
			if (actions.contains(explanandum)) {
				return error;
			}
		}

		return null;
	}

	/**
	 * Returns a list of log entries that originated within the specified time
	 * frame.
	 * 
	 * @param logEntries list of LogEntries to be filtered
	 * @param startTime  lower bound in time
	 * @param endTime    upper bound in time
	 * @return a list of log entries whose LogEntry are no younger than startTime
	 *         and no older than endTime
	 */
	public ArrayList<LogEntry> filterLogEntriesByTime(ArrayList<LogEntry> logEntries, LocalDateTime startTime,
			LocalDateTime endTime) {

		ArrayList<LogEntry> filteredLogEntries = new ArrayList<>();

		for (LogEntry logEntry : logEntries) {
			LocalDateTime logEntryDate = logEntry.getLocalDateTime();
			if (logEntryDate.isAfter(startTime) && logEntryDate.isBefore(endTime)) {
				filteredLogEntries.add(logEntry);
			}
		}

		return filteredLogEntries;
	}

}

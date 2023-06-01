package exengine.algorithmicExpGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import exengine.datamodel.Cause;
import exengine.datamodel.ErrorCause;
import exengine.datamodel.LogEntry;
import exengine.datamodel.Rule;
import exengine.datamodel.RuleCause;

@Service
public class FindCauseServiceAlternative {

	/*
	 * Differences to Algorithm 1 from the paper:
	 * 
	 * 1. The rules and log entries are naturally disjoint in this application, so
	 * no need to extract them from the explanation constructs as in the paper (Line
	 * 6 and 7).
	 * 
	 * 2. This method also finds errors (the differences appear only at the end,
	 * after every part of Algorithm 1 has already happened.
	 * 
	 * 3. Some naming, precisely:
	 * 
	 * - logEntries = X (after Line 7 was executed) - dbRules = R - path = P
	 */
	public Cause findCause(LogEntry explanandum, ArrayList<LogEntry> logEntries, List<Rule> dbRules,
			List<Error> dbErrors) {

		// Algorithm 1:

		Cause path = null; // Line 4
		Rule firedRule = null; // Line 5

		Collections.sort(logEntries, Collections.reverseOrder()); // Line 8

		ArrayList<Rule> candidateRules = findCandidateRules(explanandum, dbRules); // Line 9

		for (Rule rule : candidateRules) { // Line 10
			if (actionsApply(explanandum, rule, logEntries, 1000) // Line 11
					&& preconditionsApply(explanandum, rule)) { // Line 12 - 14
				firedRule = rule;
			}
		}

		if (firedRule != null) { // Line 15
			path = new RuleCause(null, null, null, null); // Line 16 TODO
			return path;
		}

		// Error-Handling Plug In:

		if (isError(explanandum, dbErrors)) {
			path = new ErrorCause(null, null, null, null); // TODO
			return path;
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

	public boolean actionsApply(LogEntry explanandum, Rule rule, ArrayList<LogEntry> logEntries, int tolerance) {
		LocalDateTime explanandumTime = explanandum.getLocalDateTime();
		LocalDateTime startTime = explanandumTime.minusSeconds(tolerance);
		LocalDateTime endTime = explanandumTime.plusSeconds(tolerance);
		ArrayList<LogEntry> filteredLogEntries = filterLogEntriesByTime(logEntries, startTime, endTime);
		
		return filteredLogEntries.containsAll(rule.getActions());
	}

	boolean preconditionsApply(LogEntry explanandum, Rule rule) { // TODO
		return false;
	}

	boolean isError(LogEntry explanandum, List<Error> errors) { // TODO
		return false;
	}

}

package exengine.algorithmicExpGenerator;

import java.util.ArrayList;
import java.util.List;

import exengine.datamodel.Cause;
import exengine.datamodel.Error;
import exengine.datamodel.ErrorCause;
import exengine.datamodel.LogEntry;
import exengine.datamodel.Rule;
import exengine.datamodel.RuleCause;

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
	 *  - logEntries = X (after Line 7 was executed)
	 *  - dbRules = R
	 *  - path = P
	 */
	public Cause findCause(LogEntry explanandum, ArrayList<LogEntry> logEntries, List<Rule> dbRules,
			List<Error> dbErrors) {

		// Algorithm 1:

		Cause path = null; // Line 4
		Rule firedRule = null; // Line 5

		logEntries.sort(null); // Line 8, TODO implement sortable interface to LogEntry class

		ArrayList<Rule> candidateRules = findCandidateRules(explanandum, dbRules); // Line 9

		for (Rule rule : candidateRules) { // Line 10
			if (actionsApply(explanandum, rule) // Line 11
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

	ArrayList<Rule> findCandidateRules(LogEntry explanandum, List<Rule> dbRules) { // TODO
		ArrayList<Rule> candidateRules = null;
		return candidateRules;
	}

	boolean actionsApply(LogEntry explanandum, Rule rule) { // TODO
		return false;
	}

	boolean preconditionsApply(LogEntry explanandum, Rule rule) { // TODO
		return false;
	}

	boolean isError(LogEntry explanandum, List<Error> errors) { // TODO
		return false;
	}

}

package exengine.algorithmicExpGenerator;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import exengine.datamodel.RuleCause;
import exengine.datamodel.LogEntry;
import exengine.datamodel.Rule;
import exengine.datamodel.Cause;
import exengine.datamodel.Error;
import exengine.datamodel.ErrorCause;

/**
 * Component used to find causality paths.
 */
@Service
public class FindCauseService {

	private static final Logger logger = LoggerFactory.getLogger(FindCauseService.class);

	private boolean oneORSatisfied;
	private LogEntry trigger;

	public Cause findCause(ArrayList<LogEntry> logEntries, List<Rule> dbRules, List<Error> dbErrors,
			ArrayList<String> entityIds) {

		// check if explanation shall be given for particular device
		if (entityIds != null) {
			// shorten the log accordingly
			LogEntry latestEntry = logEntries.get(logEntries.size() - 1);
			while (!entityIds.contains(latestEntry.getEntityId())) {
				logEntries.remove(latestEntry);
				latestEntry = logEntries.get(logEntries.size() - 1);
			}
		}

		Cause cause = null;
		ArrayList<String> foundRuleActions = new ArrayList<>();
		ArrayList<String> foundRuleNames = new ArrayList<>();

		/*
		 * START OF THE ALGORITHM
		 */

		// iterate through Log entries in reversed order
		for (int i = logEntries.size() - 1; i >= 0; i--) {

			String entryData = logEntries.get(i).getName() + " " + logEntries.get(i).getState();

			logger.trace("EntryData number {}: {}", i, entryData);

			// error-if case before?
			if (isInErrorActions(entryData, dbErrors) && foundRuleActions.isEmpty()) {
				// need to check for empty rule action list to be sure the error is the event to
				// be explained (because no rule was triggered after the error occurred)
				logger.debug("Return error Cause");
				return getErrorCause(logEntries, i, dbErrors);
			}

			if (isInRuleActions(entryData, dbRules)) { // if it is an action
				logger.debug("found rule action: {}", entryData);
				foundRuleActions.add(entryData); // store in action list

			} else if (isInRules(entryData, dbRules)) { // if it is a rule

				if (foundRuleActions.isEmpty()) {
					

				} else { // case: we found an action before
					logger.debug("found rule: {}", entryData);
					foundRuleNames.add(entryData);
					boolean areFoundActionsSubsetOfRuleActions = true;
					Rule foundRule = null;
					for (Rule r : dbRules) { // query db for Rule

						if ((r.getRuleEntry().getName() + " " + r.getRuleEntry().getState()).equals(entryData)) {

							foundRule = r;
							// r is the rule we want to get the actions of

							areFoundActionsSubsetOfRuleActions = checkIfFoundActionsAreSubsetOfRuleActions(
									foundRuleActions, r);

						}

					}

					if (areFoundActionsSubsetOfRuleActions) {

						logger.debug("Found actions are subset of rule actions");

						if (foundRule != null)
							oneORSatisfied = triggerConditionCheck(i, foundRule, logEntries);
						if (oneORSatisfied && (foundRule != null)) {
							cause = new RuleCause(trigger, foundRule.getConditions(), foundRule.getActions(),
									foundRule);

						}
					}

				} // closing case: we found an action before
			}
		}

		return cause;
	}

	public ErrorCause getErrorCause(ArrayList<LogEntry> logEntries, int line, List<Error> dbErrors) {
		ErrorCause errorCause = null;
		LogEntry actionEntry = logEntries.get(line);
		for (Error e : dbErrors) {
			for (LogEntry action : e.getActions()) {
				if ((action.getName() + " " + action.getState())
						.equals(actionEntry.getName() + " " + actionEntry.getState())) {
					return new ErrorCause(e.getActions(), e.getImplication(), e.getSolution(), e);
				}
			}
		}
		return errorCause;
	}

	// checks if entry data string is in rule actions
	public boolean isInRuleActions(String toCheck, List<Rule> dbRules) {
		boolean result = false;
		// check for rule actions
		for (Rule r : dbRules) {
			for (LogEntry action : r.getActions())
				if ((action.getName() + " " + action.getState()).equals(toCheck)) {
					result = true;
				}
		}
		return result;
	}

	// checks if entry data string is in error actions
	public boolean isInErrorActions(String toCheck, List<Error> dbErrors) {
		boolean result = false;
		// check for error actions
		for (Error e : dbErrors) {
			for (LogEntry action : e.getActions())
				if ((action.getName() + " " + action.getState()).equals(toCheck)) {
					result = true;
				}
		}
		return result;
	}

	public boolean isInRules(String toCheck, List<Rule> dbRules) {
		boolean result = false;
		for (Rule r : dbRules) {
			if ((r.getRuleEntry().getName() + " " + r.getRuleEntry().getState()).equals(toCheck)) {
				result = true;
			}
		}
		return result;
	}

	public boolean checkIfFoundActionsAreSubsetOfRuleActions(ArrayList<String> foundActions, Rule r) {
		boolean areFoundActionsSubsetOfRuleActions = true;
		for (String foundAction : foundActions) {

			// flag if current found Action is part of the rule
			boolean isfoundActionPartOfRule = false;

			// iterating through actions of Rule
			for (LogEntry ruleAction : r.getActions()) {

				// if we find a Rule-action that matches, set part-of-rule-flag to true
				if ((ruleAction.getName() + " " + ruleAction.getState()).equals(foundAction)) {
					isfoundActionPartOfRule = true;
				}

			}

			// if we didn't find any Rule-action, set the subset flag to false
			if (!isfoundActionPartOfRule) {
				areFoundActionsSubsetOfRuleActions = false;
			}

		} // closing for loop (iterating through found actions)
		return areFoundActionsSubsetOfRuleActions;
	}

	public boolean triggerConditionCheck(int line, Rule r, ArrayList<LogEntry> logEntries) {
		if (r.getTrigger() == null)
			return true;
		for (int i = line; i >= 0; i--) { // go back from line
			for (LogEntry t : r.getTrigger()) // look for last trigger
				if ((logEntries.get(i).getName() + " " + logEntries.get(i).getState())
						.equals(t.getName() + " " + t.getState())) {
					trigger = logEntries.get(i);
					return true;
				}

		}
		return false;
	}

	public boolean isEntryInList(LogEntry entry, ArrayList<String> entityIds) {
		boolean result = false;
		for (String entityId : entityIds) {
			if (entry.getEntityId() == entityId) {
				result = true;
			}
		}
		return result;
	}

}

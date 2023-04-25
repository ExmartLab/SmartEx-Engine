package exengine.algorithmicExpGenerator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import exengine.ExplainableEngineApplication;
import exengine.datamodel.RuleCause;
import exengine.datamodel.LogEntry;
import exengine.datamodel.Rule;
import exengine.datamodel.Cause;
import exengine.datamodel.Error;
import exengine.datamodel.ErrorCause;

@Service
public class FindCauseService {

	private boolean oneORSatisfied;
	private LogEntry trigger;

	public Cause findCause(ArrayList<LogEntry> logEntries, List<Rule> dbRules, List<Error> dbErrors, ArrayList<String> entityIds) {
		
		// check if explanation shall be given for particular device
		if (entityIds != null) {
			// shorten the log accordingly
			LogEntry latestEntry = logEntries.get(logEntries.size() - 1);
			while (!entityIds.contains(latestEntry.entity_id)) {
				if (ExplainableEngineApplication.isDebug()) {
					System.out.println("Remove logEntry item");
				}
				logEntries.remove(latestEntry);
				latestEntry = logEntries.get(logEntries.size() - 1);
			}
		}
		
		
		Cause cause = null;
			
		for (LogEntry l : logEntries)
			if (ExplainableEngineApplication.isDebug())
				System.out.println(l.toString());

		// initialize lists for actions and rules from Logs
		ArrayList<String> foundRuleActions = new ArrayList<String>();
		ArrayList<String> foundRuleNames = new ArrayList<String>();

		/*
		 * START OF THE ALGORITHM
		 */

		// iterate through Log Entries in reversed order
		for (int i = logEntries.size() - 1; i >= 0; i--) { // read each line

			//
			String entryData = logEntries.get(i).getName() + " " + logEntries.get(i).getState();
			if (ExplainableEngineApplication.isDebug())
				System.out.println(i + ":\n" + entryData);

			// error-if case before?
			if (isInErrorActions(entryData, dbErrors) && foundRuleActions.isEmpty()) {
				// need to check for empty rule action list to be sure the error is the event to
				// be explained (because no rule was triggered after the error occurred)
				System.out.println("returning error Cause");
				return getErrorCause(logEntries, i, dbErrors);
			}

			if (isInRuleActions(entryData, dbRules)) { // if it is an action
				if (ExplainableEngineApplication.isDebug())
					System.out.println("found rule action: " + entryData);
				foundRuleActions.add(entryData); // store in action list

			} else if (isInRules(entryData, dbRules)) { // if it is a rule

				if (foundRuleActions.isEmpty()) {
					continue;

				} else { // case: we found an action before
					if (ExplainableEngineApplication.isDebug())
						System.out.println("found rule: " + entryData);
					foundRuleNames.add(entryData);
					boolean areFoundActionsSubsetOfRuleActions = true;
					Rule foundRule = null;
					for (Rule r : dbRules) { // query db for Rule

						if ((r.getRuleEntry().name + " " + r.getRuleEntry().state).equals(entryData)) {

							foundRule = r;
							// r is the rule we want to get the actions of

							areFoundActionsSubsetOfRuleActions = checkIfFoundActionsAreSubsetOfRuleActions(
									foundRuleActions, r);

						}

					}

					if (areFoundActionsSubsetOfRuleActions) {

						if (ExplainableEngineApplication.isDebug())
							System.out.println("found actoins are subset of rule actions");

						if (foundRule != null)
							oneORSatisfied = triggerConditionCheck(i, foundRule, logEntries);
						if (oneORSatisfied) {
							if (foundRule != null) {
								cause = new RuleCause(trigger, foundRule.getConditions(), foundRule.getActions(),
										foundRule);
							}
						}
					}

				} // closing case: we found an action before

			} else {
				continue;
			}
		}

		return cause;
	}

	public ErrorCause getErrorCause(ArrayList<LogEntry> logEntries, int line, List<Error> dbErrors) {
		ErrorCause errorCause = null;
		LogEntry actionEntry = logEntries.get(line);
		for(Error e : dbErrors)
		{
			for(LogEntry action : e.actions) {
				if((action.name + " " + action.state).equals(actionEntry.name + " " + actionEntry.state)) {
					return new ErrorCause(e.actions, e.implication, e.solution, e);
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
				if ((action.name + " " + action.state).equals(toCheck)) {
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
				if ((action.name + " " + action.state).equals(toCheck)) {
					result = true;
				}
		}
		return result;
	}

	public boolean isInRules(String toCheck, List<Rule> dbRules) {
		boolean result = false;
		for (Rule r : dbRules) {
			if ((r.getRuleEntry().name + " " + r.getRuleEntry().state).equals(toCheck)) {
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
			for (LogEntry ruleAction : r.actions) {

				// if we find a Rule-action that matches, set part-of-rule-flag to true
				if ((ruleAction.name + " " + ruleAction.state).equals(foundAction)) {
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
				if ((logEntries.get(i).name + " " + logEntries.get(i).getState()).equals(t.name + " " + t.state)) {
					trigger = logEntries.get(i);
					return true;
				}

		}
		return false;
	}
	
	public boolean isEntryInList(LogEntry entry, ArrayList<String> entityIds) {
		boolean result = false;		
		for (String entityId: entityIds) {
			if (entry.entity_id == entityId) {
				result = true;
			}
		}
		return result;
	}

}

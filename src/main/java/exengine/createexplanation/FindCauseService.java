package exengine.createexplanation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import exengine.datamodel.Cause;
import exengine.datamodel.LogEntry;
import exengine.datamodel.Rule;

@Service
public class FindCauseService {

	private boolean oneORSatisfied;
	private LogEntry trigger;
	
	boolean debug = true;

	public Cause findCause(ArrayList<LogEntry> logEntries, List<Rule> dbRules) {
		Cause cause = null;

		for (LogEntry l : logEntries)
			System.out.println(l.toString());

		// initialize lists for actions and rules from Logs
		ArrayList<String> foundActions = new ArrayList<String>();
		ArrayList<String> foundRuleNames = new ArrayList<String>();

		/*
		 * START OF THE ALGORITHM
		 */
		
		if(debug)
			System.out.println("----- STARTING CAUSE ALGORITHM ------");

		// iterate through Log Entries in reversed order
		for (int i = logEntries.size() - 1; i >= 0; i--) { // read each line

			String entryData = logEntries.get(i).getName() + " " + logEntries.get(i).getState();
			if(debug)
				System.out.println(i + ":\n" + entryData);

			if (isInActions(entryData, dbRules)) { // if it is an action
				if(debug)
					System.out.println("found action: " + entryData);
				foundActions.add(entryData); // store in action list

			} else if (isInRules(entryData, dbRules)) { // if it is a rule

				if (foundActions.isEmpty()) {
					continue;

				} else { // case: we found an action before
					if(debug)
						System.out.println("found rule: " + entryData);
					foundRuleNames.add(entryData);
					boolean areFoundActionsSubsetOfRuleActions = true;
					Rule foundRule = null;
					for (Rule r : dbRules) { // query db for Rule

						if ((r.getRuleEntry().name + " " + r.getRuleEntry().state).equals(entryData)) {

							foundRule = r;
							// r is the rule we want to get the actions of

							areFoundActionsSubsetOfRuleActions = checkIfFoundActionsAreSubsetOfRuleActions(foundActions,
									r);

						}

					}

					if (areFoundActionsSubsetOfRuleActions) {
						
						if(debug)
							System.out.println("found actoins are subset of rule actions");
						
						if (foundRule != null)
							oneORSatisfied = triggerConditionCheck(i, foundRule, logEntries);
						if (oneORSatisfied) {
							if (foundRule != null) {
								cause = new Cause(trigger, foundRule.getConditions(), foundRule.getActions(),
										foundRule);
//								System.out.println("actions: " + foundRule.getActionsString()
//										+ "\ntrigger: " + trigger
//										+ "\nconditions: " + foundRule.getConditionsString()
//										+ "\nrule: " + foundRule.getRuleName());

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

	public boolean isInActions(String toCheck, List<Rule> dbRules) {
		boolean result = false;
		for (Rule r : dbRules) {
			for (LogEntry action : r.getActions())
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

}

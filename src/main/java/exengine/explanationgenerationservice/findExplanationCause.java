package exengine.explanationgenerationservice;

import java.util.ArrayList;
import java.util.List;


import exengine.datamodel.Rule;
import exengine.haconnection.LogEntry;

public class findExplanationCause {

	private static boolean oneORSatisfied;
	private static String trigger;

	public static Cause findCause(ArrayList<LogEntry> logEntries, List<Rule> dbRules) {
		Cause cause = null;
		
		for (LogEntry l : logEntries)
			System.out.println(l.toString());

		// initialize lists for actions and rules from Logs
		ArrayList<String> foundActions = new ArrayList<String>();
		ArrayList<String> foundRuleNames = new ArrayList<String>();

		/*
		 * START OF THE ALGORITHM
		 */

		// iterate through Log Entries in reversed order
		for (int i = logEntries.size() - 1; i >= 0; i--) { // read each line

			String entryData = logEntries.get(i).getName() + " " + logEntries.get(i).getState();
//			System.out.println(entryData);

			if (isInActions(entryData, dbRules)) { // if it is an action

				System.out.println("found action: " + entryData);
				foundActions.add(entryData); // store in action list

			} else if (isInRules(entryData, dbRules)) { // if it is a rule

				if (foundActions.isEmpty()) {
					continue;

				} else { // case: we found an action before

					System.out.println("found rule: " + entryData);
					foundRuleNames.add(entryData);
					boolean areFoundActionsSubsetOfRuleActions = true;
					Rule foundRule = null;
					for (Rule r : dbRules) { // query db for Rule

						if (r.getRuleName().equals(entryData)) {

							foundRule = r;
							// r is the rule we want to get the actions of

							areFoundActionsSubsetOfRuleActions = checkIfAreFoundActionsSubsetOfRuleActions(foundActions,
									r);

						}

					}

					if (areFoundActionsSubsetOfRuleActions) {

						oneORSatisfied = triggerConditionCheck(i, foundRule, logEntries);
						if (oneORSatisfied) {
							if (foundRule != null) {
									
								cause = new Cause(trigger, foundRule.getConditions(), foundRule.getActions(), foundRule.getRuleName());
								
								System.out.println("actions: " + foundRule.getActionsString()
										+ "\ntrigger: " + trigger
										+ "\nconditions: " + foundRule.getConditionsString()
										+ "\nrule: " + foundRule.getRuleName());
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
	
	public static boolean isInActions(String toCheck, List<Rule> dbRules) {
		boolean result = false;
		for (Rule r : dbRules) {
			for (String action : r.getActions())
				if (action.equals(toCheck)) {
					result = true;
				}
		}
		return result;
	}

	public static boolean isInRules(String toCheck, List<Rule> dbRules) {
		boolean result = false;
		for (Rule r : dbRules) {
			if (r.getRuleName().equals(toCheck)) {
				result = true;
			}
		}
		return result;
	}

	public static boolean checkIfAreFoundActionsSubsetOfRuleActions(ArrayList<String> foundActions, Rule r) {
		boolean areFoundActionsSubsetOfRuleActions = true;
		for (String foundAction : foundActions) {

			// flag if current found Action is part of the rule
			boolean isfoundActionPartOfRule = false;

			// iterating through actions of Rule
			for (String ruleAction : r.actions) {

				// if we find a Rule-action that matches, set part-of-rule-flag to true
				if (foundAction.equals(ruleAction)) {
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

	public static boolean triggerConditionCheck(int line, Rule r, ArrayList<LogEntry> logEntries) {
		if(r.getTrigger()==null)
			return true;
		for (int i = line; i >= 0; i--) { // go back from line
			for (String t : r.getTrigger()) //look for last trigger
				if ((logEntries.get(i).name + " " + logEntries.get(i).getState()).equals(t)) {
					trigger = t;
					return true;
				}
					
		}
		return false;
	}

	
}

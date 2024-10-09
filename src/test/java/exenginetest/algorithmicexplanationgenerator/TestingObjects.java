package exenginetest.algorithmicexplanationgenerator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import exengine.ExplainableEngineApplication;
import exengine.datamodel.Error;
import exengine.datamodel.LogEntry;
import exengine.datamodel.Rule;
import exengine.loader.JsonHandler;

/*
 * Loads Objects containing testing data and testing data only.
 * 
 * This class shall be used to test the explanation generation process. 
 * 
 * For this purpose, we do not use the actual database for testing since we want the unit tests 
 * to be uncoupled from the database. Thereby, testing with this data does not require to have 
 * the database always in a state that contains the test data.
 */
public class TestingObjects {

	private ArrayList<LogEntry> demoEntries;
	private ArrayList<Rule> dbRules;
	private ArrayList<Error> dbErrors;

	public TestingObjects() throws IOException, URISyntaxException {
		// populate demoEntries
		String logJSON = JsonHandler.loadFile("testingData/" + ExplainableEngineApplication.FILE_NAME_DEMO_LOGS);
		demoEntries = JsonHandler.loadLogEntriesFromJson(logJSON);

		populateRules();
		populateErrors();
	}

	private void populateRules() {
		dbRules = new ArrayList<Rule>();

		ArrayList<LogEntry> triggers;
		ArrayList<LogEntry> conditions;
		ArrayList<LogEntry> actions;

		triggers = new ArrayList<LogEntry>();
		triggers.add(demoEntries.get(3));
		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(5));
		conditions = new ArrayList<LogEntry>(); // String time, String name, String state, String entityId,
												// ArrayList<String> other

		//
		conditions.add(new LogEntry(null, "daily energy consumption is higher than the threshold", null, null, null));
		dbRules.add(new Rule("rule 1 (coffee)", "1", triggers, conditions, actions, "1",
				"Rule_1: allows coffee to be made only until the daily energy consumption threshold is reached", 1));

		triggers = new ArrayList<LogEntry>();
		triggers.add(demoEntries.get(7));
		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(10));
		conditions = new ArrayList<LogEntry>();
		conditions.add(new LogEntry(null, "a meeting in room 1 is going on", null, null, null));

		dbRules.add(new Rule("rule 2 (tv mute)", "2", triggers, conditions, actions, "2",
				"Rule_2: mutes the TV if TV is playing while a meeting is going on", 2));

		// synthetic rule (same action as "rule 2 (tv mute)" but different triggers:
		triggers = new ArrayList<LogEntry>();
		triggers.add(demoEntries.get(3));
		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(10));
		conditions = new ArrayList<LogEntry>();
		conditions.add(new LogEntry(null, "a made up condition", null, null, null));

		dbRules.add(new Rule("rule 3 (constructed rule)", "3", triggers, conditions, actions, "2",
				"Rule_3: a constructed rule for testing purposes", 3));

		// synthetic rule (same action as "rule 2 (tv mute)" but different triggers, one
		// of which is never satisfied in the demo log (to test that the actions of this
		// rule do not apply):
		triggers = new ArrayList<LogEntry>();
		triggers.add(demoEntries.get(3));

		LogEntry neverSatisfiedTrigger = new LogEntry(null, "Never triggered alarm", "on", "alarm_fire", null);
		triggers.add(neverSatisfiedTrigger);

		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(10));

		LogEntry neverSatisfiedAction = new LogEntry(null, "Never used strobo light", "on", "light_strobo", null);
		actions.add(neverSatisfiedAction);

		conditions = new ArrayList<LogEntry>();
		conditions.add(new LogEntry(null, "a made up condition", null, null, null));

		dbRules.add(new Rule("rule 4 (constructed rule)", "4", triggers, conditions, actions, "4",
				"Rule_4: a constructed rule for testing purposes to verify that this rule's actions were never performed", 5));

		// synthetic rule (same action as "rule 2 (tv mute)" but never triggered:
		triggers = new ArrayList<LogEntry>();
		triggers.add(neverSatisfiedTrigger);
		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(10));
		conditions = new ArrayList<LogEntry>();
		conditions.add(new LogEntry(null, "a made up condition", null, null, null));

		dbRules.add(new Rule("rule 5 (constructed rule)", "5", triggers, conditions, actions, "5",
				"Rule_5: a constructed rule for testing purposes, never to be triggered", 6));


		/** Rules for testing Counterfactual Explanation Service:*/
		//get this rule by dbRules.get(5)
		//makes dbRules.get(7) fire
		conditions = new ArrayList<LogEntry>();
		conditions.add(demoEntries.get(28));
		triggers = new ArrayList<LogEntry>();
		triggers.add(demoEntries.get(24));
		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(17));
		actions.add(demoEntries.get(25));

		dbRules.add(new Rule("rule for counterfactual testing 1 (index 5) (constructed rule)", "6", triggers, conditions, actions, "2",
				"Rule_9: a constructed rule for testing with true preconditions and true triggers if the explanandum is after the demoEntry with index 21", 10));

		//get this rule by dbRules.get(6)
		conditions = new ArrayList<LogEntry>();
		conditions.add(demoEntries.get(21));
		conditions.add(demoEntries.get(17));
		triggers = new ArrayList<LogEntry>();
		triggers.add(demoEntries.get(16));
		triggers.add(demoEntries.get(20));
		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(19));
		dbRules.add(new Rule("rule for counterfactual testing 2 (index 6) (constructed rule)", "7", null, conditions, actions, "2",
				"Rule_10: a constructed rule for testing with false preconditions but true triggers if the explanandum is after the demoEntry with index 13", 9));

		//get this rule by dbRules.get(7)
		//fired by dbRule.get(5)
		conditions = new ArrayList<LogEntry>();
		conditions.add(demoEntries.get(17));
		conditions.add(demoEntries.get(25));
		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(26));
		dbRules.add(new Rule("rule for counterfactual testing 3 (index 7) (constructed rule)", "8", null, conditions, actions, "2",
				"Rule_11: a constructed rule for testing which is fired by the rule with index 5", 8));

		//get this rule by dbRules.get(8)
		conditions = new ArrayList<LogEntry>();
		conditions.add(demoEntries.get(27));
		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(19));
		dbRules.add(new Rule("rule for counterfactual testing 4 (index 8) (constructed rule)", "9", null, conditions, actions, "2",
				"Rule_12: a constructed rule which has the same action as the rule with index 6", 7));


		//get this rule by dbRules.get(9)
		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(17));
		actions.add(demoEntries.get(21));
		conditions = new ArrayList<LogEntry>();
		conditions.add(demoEntries.get(22));
		dbRules.add(new Rule("rule for counterfactual testing 5 (index 9) (constructed rule)", "10", null, conditions, actions, "2",
				"Rule_13: a constructed rule which can make the rule with index 6 fire", 11));











	}

	private void populateErrors() {
		dbErrors = new ArrayList<Error>();

		ArrayList<LogEntry> actions;

		// populate errors
		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(2));
		dbErrors.add(new Error("No. 104: DEEBOT gets stuck while working and stops", "04", actions,
				"DEEBOT is tangled with something on the floor or might be stuck under furniture",
				"If DEEBOT can not free itself, manually remove the obstacles and restart"));
	}

	public ArrayList<LogEntry> getDemoEntries() {
		return demoEntries;
	}

	public ArrayList<Rule> getDBRules() {
		return dbRules;
	}

	public ArrayList<Error> getDBErrors() {
		return dbErrors;
	}

}

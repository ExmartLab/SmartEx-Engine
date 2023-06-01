package XLab.ExplainableEngine.algorithmicExpGenerator;

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

	TestingObjects() throws IOException, URISyntaxException {
		// populate demoEntries
		String logJSON = JsonHandler.loadFile("testingData/" + ExplainableEngineApplication.FILE_NAME_DEMO_LOGS);
		demoEntries = JsonHandler.loadLogEntriesFromJson(logJSON);

		populateRules();
		populateErrors();
	}

	private void populateRules() {
		dbRules = new ArrayList<Rule>();

		ArrayList<LogEntry> triggers;
		ArrayList<String> conditions;
		ArrayList<LogEntry> actions;

		triggers = new ArrayList<LogEntry>();
		triggers.add(demoEntries.get(3));
		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(5));
		conditions = new ArrayList<String>();
		conditions.add("daily energy consumption is higher than the threshold");
		dbRules.add(new Rule("rule 1 (coffee)", "1", demoEntries.get(4), triggers, conditions, actions, "1",
				"Rule_1: allows coffee to be made only until the daily energy consumption threshold is reached"));

		triggers = new ArrayList<LogEntry>();
		triggers.add(demoEntries.get(7));
		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(9));
		conditions = new ArrayList<String>();
		conditions.add("a meeting in room 1 is going on");

		dbRules.add(new Rule("rule 2 (tv mute)", "2", demoEntries.get(8), triggers, conditions, actions, "2",
				"Rule_2: mutes the TV if TV is playing while a meeting is going on"));

		// synthetic rule (same action as "rule 2 (tv mute)" but different triggers:
		triggers = new ArrayList<LogEntry>();
		triggers.add(demoEntries.get(3));
		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(9));
		conditions = new ArrayList<String>();
		conditions.add("a made up condition");

		dbRules.add(new Rule("rule 3 (constructed rule)", "3", demoEntries.get(8), triggers, conditions, actions, "2",
				"Rule_3: a constructed rule for testing purposes"));

		// synthetic rule (same action as "rule 2 (tv mute)" but different triggers, one
		// of which is never satisfied in the demo log (to test that the actions of this
		// rule do not apply):
		triggers = new ArrayList<LogEntry>();
		triggers.add(demoEntries.get(3));
		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(9));
		
		LogEntry neverSatisfied = new LogEntry();
		neverSatisfied.setName("Never used strobo light");
		neverSatisfied.setEntityId("light_strobo");
		neverSatisfied.setState("on");
		
		actions.add(neverSatisfied);
		conditions = new ArrayList<String>();
		conditions.add("a made up condition");

		dbRules.add(new Rule("rule 4 (constructed rule)", "4", demoEntries.get(8), triggers, conditions, actions, "4",
				"Rule_4: a constructed rule for testing purposes to verify that this rule's actions were never performed"));
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

	ArrayList<LogEntry> getDemoEntries() {
		return demoEntries;
	}

	ArrayList<Rule> getDBRules() {
		return dbRules;
	}

	ArrayList<Error> getDBErrors() {
		return dbErrors;
	}

}

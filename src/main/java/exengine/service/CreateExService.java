package exengine.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.database.RuleRepository;
import exengine.datamodel.Rule;
import exengine.haconnection.HA_API;
import exengine.haconnection.LogEntry;

@Service
public class CreateExService {

	private List<Rule> dbRules;

	private boolean oneORSatisfied;
	private String trigger;

	private ArrayList<LogEntry> logEntries;

	@Autowired
	private RuleRepository ruleRepo;

	ArrayList<LogEntry> demoEntries = new ArrayList<LogEntry>();

	public String getExplanation(int min, String userId, String userState, String userLocation) {

		// turned off for testing
//		logEntries = HA_API.parseLastLogs(min);

		// only for testing
		int scenario = 2;
		System.out.println("Demo for Scenario " + scenario);
		initiateDemoEntries(scenario);
		logEntries = demoEntries;

		// default value for return string
		String explanation = "found nothing to explain";

		for (LogEntry l : logEntries)
			System.out.println(l.toString());

		// initialize lists for actions and rules from Logs
		ArrayList<String> foundActions = new ArrayList<String>();
		ArrayList<String> foundRuleNames = new ArrayList<String>();

		// query Rules from DB
		dbRules = findRules();

		/*
		 * START OF THE ALGORITHM
		 */

		// iterate through Log Entries in reversed order
		for (int i = logEntries.size() - 1; i >= 0; i--) { // read each line

			String entryData = logEntries.get(i).getName() + " " + logEntries.get(i).getState();
//			System.out.println(entryData);

			if (isInActions(entryData)) { // if it is an action

				System.out.println("found action: " + entryData);
				foundActions.add(entryData); // store in action list

			} else if (isInRules(entryData)) { // if it is a rule

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

						oneORSatisfied = triggerConditionCheck(i, foundRule);
						if (oneORSatisfied) {
							if (foundRule != null) {
								
								/* TODO
								 * 
								 * Check which explanation has to be given (with Context)
								 * 
								 */
								
								
								explanation = "actions: " + foundRule.getActionsString()
										+ "\ntrigger: " + trigger
										+ "\nconditions: " + foundRule.getConditionsString()
										+ "\nrule: " + foundRule.getRuleName();
							}
							System.out.println(explanation);
						}
					}

				} // closing case: we found an action before

			} else {
				continue;
			}
		}
		return explanation;
	}

	public boolean isInActions(String toCheck) {
		boolean result = false;
		for (Rule r : dbRules) {
			for (String action : r.getActions())
				if (action.equals(toCheck)) {
					result = true;
				}
		}
		return result;
	}

	public boolean isInRules(String toCheck) {
		boolean result = false;
		for (Rule r : dbRules) {
			if (r.getRuleName().equals(toCheck)) {
				result = true;
			}
		}
		return result;
	}

	public List<Rule> findRules() {
		return ruleRepo.findAll();
	}

	public boolean checkIfAreFoundActionsSubsetOfRuleActions(ArrayList<String> foundActions, Rule r) {
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

	public boolean triggerConditionCheck(int line, Rule r) {
		if(r.getTrigger()==null)
			return true;
		for (int i = line; i >= 0; i--) { // go back from line
			for (String trigger : r.getTrigger()) //look for last trigger
				if ((logEntries.get(i).name + " " + logEntries.get(i).getState()).equals(trigger)) {
					this.trigger = trigger;
					return true;
				}
					
		}
		return false;
	}

	/*
	 * public List<Rule> findRulesByName() { return
	 * ruleRepo.findByRuleName("testRule1"); }
	 */

	public void initiateDemoEntries(int scenario) {
		demoEntries = new ArrayList<LogEntry>();
		ArrayList<String> other;
		switch (scenario) {
		case 1:
			other = new ArrayList<String>();
			other.add("message\": \"triggered by state of sensor.smart_plug_social_room_coffee_today_s_consumption");
			other.add("source\": \"state of sensor.smart_plug_social_room_coffee_today_s_consumption");
			other.add("context_id\": \"01G67ZHDBKS302M9XP2GJTZAJH\", \"domain\": \"automation");
			demoEntries.add(new LogEntry("2022-06-23T09:50:50.229746+00:00", "sc1: Goal-Order-Conflict", null,
					"automation.test_scenario_watching_tv_light_off", other));

			other = new ArrayList<String>();
			other.add("context_event_type\": \"automation_triggered");
			other.add("context_domain\": \"automation");
			other.add("context_name\": \"sc1: Goal-Order-Conflict");
			other.add(
					"context_message\": \"triggered by state of sensor.smart_plug_social_room_coffee_today_s_consumption");
			other.add("context_source\": \"state of sensor.smart_plug_social_room_coffee_today_s_consumption");
			other.add("context_entity_id\": \"automation.test_scenario_watching_tv_light_off");
			other.add("context_entity_id_name\": \"sc1: Goal-Order-Conflict");
			demoEntries.add(new LogEntry("2022-06-23T09:50:50.848452+00:00", "Smart Plug Social Room Coffee", "off",
					"switch.smart_plug_social_room_coffee", other));
			break;

		case 2:
			other = new ArrayList<String>();
			other.add("context_event_type\": \"automation_triggered");
			other.add("context_domain\": \"automation");
			other.add("context_name\": \"Welcome");
			other.add("context_message\": \"triggered by state of binary_sensor.door");
			other.add("context_source\": \"state of binary_sensor.door");
			other.add("context_entity_id\": \"automation.presence_notification");
			other.add("context_entity_id_name\": \"Welcome");
			demoEntries.add(
					new LogEntry("2022-06-23T11:19:30.181206+00:00", "Lab TV", "idle", "media_player.lab_tv", other));

			demoEntries.add(
					new LogEntry("2022-06-23T11:19:31.024951+00:00", "Lab TV", "playing", "media_player.lab_tv", null));

			other = new ArrayList<String>();
			other.add("message\": \"triggered by state of media_player.lab_tv");
			other.add("source\": \"state of media_player.lab_tv");
			other.add("context_id\": \"01G684KSEJHD3DRWH9K36578E9");
			other.add("domain\": \"automation");
			other.add("context_state\": \"playing");
			other.add("context_entity_id\": \"media_player.lab_tv");
			other.add("context_entity_id_name\": \"Lab TV");
			demoEntries.add(new LogEntry("2022-06-23T11:19:31.028089+00:00", "sc2: Multi-User-Conflict", "null",
					"automation.sc2_multi_user_conflict", other));

			other = new ArrayList<String>();
			other.add("icon\": \"mdi:television");
			other.add("context_event_type\": \"automation_triggered");
			other.add("context_domain\": \"automation");
			other.add("context_name\": \"sc2: Multi-User-Conflict");
			other.add("context_message\": \"triggered by state of media_player.lab_tv");
			other.add("context_source\": \"state of media_player.lab_tv");
			other.add("context_entity_id\": \"automation.sc2_multi_user_conflict");
			other.add("context_entity_id_name\": \"sc2: Multi-User-Conflict");
			demoEntries
					.add(new LogEntry("2022-06-23T11:19:31.037231+00:00", "tv_mute", null, "scene.tv_playing", other));
			break;

		case 4:

			break;

		case 5:
			demoEntries.add(new LogEntry("2022-06-23T09:07:26.920189+00:00", "Deebot", "idle", "vacuum.deebot", null));
			demoEntries.add(new LogEntry("2022-06-23T09:07:26.932243+00:00", "Deebot", "error", "vacuum.deebot", null));

			other = new ArrayList<String>();
			other.add("icon\": \"mdi:alert-circle");
			demoEntries.add(new LogEntry("2022-06-23T09:07:26.933444+00:00", "Deebot last error", "104",
					"sensor.deebot_last_error", other));

			break;
		}
	}

}
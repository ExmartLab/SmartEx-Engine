package exengine.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.Cause;
import exengine.findExplanationCause;
import exengine.database.RuleRepository;
import exengine.datamodel.Rule;
import exengine.haconnection.HA_API;
import exengine.haconnection.LogEntry;

@Service
public class CreateExService {

	

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
		String explanation = "";
		
		// query Rules from DB
		List<Rule> dbRules = findRules();

		/*
		 * STEP 1: FIND CAUSE
		 */
		
		Cause cause = findExplanationCause.findCause(logEntries, dbRules);
		if(cause == null)
			explanation = "found nothing to explain";
		else {
			/*
			 * STEP 2: TODO get context
			 */
			
			/*
			 * STEP 3: TODO ask rule engine what explanation to generate
			 */
		}
		
		return explanation;
	}


	public List<Rule> findRules() {
		return ruleRepo.findAll();
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
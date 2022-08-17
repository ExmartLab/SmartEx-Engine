package exengine.explanationgenerationservice;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.contextmanagement.ContextService;
import exengine.database.*;
import exengine.datamodel.*;
import exengine.explanationtypes.*;
import exengine.haconnection.LogEntry;
import exengine.rulebook.ExTypeOperator;

@Service
public class CreateExService {

	private ArrayList<LogEntry> logEntries;

	@Autowired
	DatabaseService dataSer;
	
	@Autowired
	FindCauseService findCauseSer;
	
	@Autowired
	ContextService conSer;

	ArrayList<LogEntry> demoEntries = new ArrayList<LogEntry>();

	public String getExplanation(int min, int userId, String userState, String userLocation) {

		//test for valid userId
		/*
		 * TODO test if user with userId is in db
		 */
		if(userId == 0)
			return "unvalid userId";
			
		// turned off for testing
//		logEntries = HA_API.parseLastLogs(min);

		// only for testing
		int scenario = 2;
		System.out.println("Demo for Scenario " + scenario);
		initiateDemoEntries(scenario);
		logEntries = demoEntries;

		// default value for return string
		String explanation = "no explanation found";

		// query Rules from DB
		List<Rule> dbRules = dataSer.findAllRules();

		/*
		 * STEP 1: FIND CAUSE
		 */
		Cause cause = findCauseSer.findCause(logEntries, dbRules);

		// return in case no cause has been found
		if (cause == null)
			return "found nothing to explain";

		/*
		 * STEP 2: TODO get context (method in class)
		 */
		//default state break
		State state = State.BREAK;
		//test for other cases
		if(userState.equals(State.WORKING.toString()))
			state = State.WORKING;
		else if(userState.equals(State.MEETING.toString()))
			state = State.MEETING;

		//Context context = conSer.getAllContext(cause, userId, state, userLocation);
		
		// for testing
		Context context = new Context(Role.GUEST, Occurrence.SECOND, Technicality.MEDTECH, State.WORKING, null);

		/*
		 * STEP 3: ask rule engine what explanation type to generate
		 */
		ExplanationType type = ExTypeOperator.getExplanationType(context);

		/*
		 * STEP 4: TODO generate the desired explanation (methods in classes) MORE
		 * CONTEXT NEEDED
		 */
		if (type == null)
			return "no explanation for given context";

		switch (type) {
		case FULLEX:
			explanation = FullExplanation.getFullExplanation(cause, context);
			break;
		case RULEEX:
			explanation = RuleExplanation.getRuleExplanation(cause, context);
			break;
		case FACTEX:
			explanation = FactExplanation.getFactExplanation(cause, context);
			break;
		case SIMPLYDEX:
			explanation = SimplyfiedExplanation.getSimplyfiedExplanation(cause, context);
			break;
		}

		return explanation;
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
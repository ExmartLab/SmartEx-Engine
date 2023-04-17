package exengine;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import exengine.database.DatabaseService;
import exengine.datamodel.*;
import exengine.datamodel.Error;
import exengine.haconnection.HomeAssistantConnectionService;

@SpringBootApplication
public class ExplainableEngineApplication implements CommandLineRunner {

	@Autowired
	private HomeAssistantConnectionService haService;

	@Autowired
	DatabaseService dataSer;

	public static boolean debug = true;
	public static boolean testing = true;

	public static ArrayList<LogEntry> demoEntries;

	public static void main(String[] args) {
		SpringApplication.run(ExplainableEngineApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if (testing) {
			deleteAllOccurrencies();
			// initializeTestOccurrenceRepository();
			//initializeTestUserRepository();
			initializeTestRuleRepository();
			initializeTestEntityRepository();
		}

		// print out current API Status to see that HA is reachable
		haService.printAPIStatus();
	}

	private void initializeTestEntityRepository() {
		dataSer.deleteAllEntities();

		Entity newEntity = new Entity("sensor.lab_fan_current_consumption", "lab_fan");
		dataSer.saveNewEntity(newEntity);

		newEntity = new Entity("switch.lab_fan", "lab_fan");
		dataSer.saveNewEntity(newEntity);

		newEntity = new Entity("sensor.door_power", "lab_door");
		dataSer.saveNewEntity(newEntity);
		
		newEntity = new Entity("switch.smart_plug_social_room_coffee", "coffee_machine");
		dataSer.saveNewEntity(newEntity);
		
		newEntity = new Entity("scene.tv_playing", "tv");
		dataSer.saveNewEntity(newEntity);

		newEntity = new Entity("sensor.deebot_last_error", "robo_cleaner");
		dataSer.saveNewEntity(newEntity);
		
		newEntity = new Entity("vacuum.deebot", "robo_cleaner");
		dataSer.saveNewEntity(newEntity);
	}

	// initializes a Repository with Users for demonstration and testing in the
	// database
	public void initializeTestUserRepository() {
		dataSer.deleteAllUsers();

		User alice = new User("Alice", "1", Role.COWORKER, Technicality.MEDTECH);
		dataSer.saveNewUser(alice);

		User bob = new User("Bob", "2", Role.COWORKER, Technicality.NONTECH);
		dataSer.saveNewUser(bob);

		User chuck = new User("Chuck", "3", Role.COWORKER, Technicality.TECHNICAL);
		dataSer.saveNewUser(chuck);

		User dana = new User("Dana", "4", Role.GUEST, Technicality.TECHNICAL);
		dataSer.saveNewUser(dana);

		// more test users, currently commented because not neccessary for test cases
//		User freyja = new User("Freyja", "5", Role.GUEST, Technicality.MEDTECH);
//		dataSer.saveNewUser(freyja);
//		
//		User grace = new User("Grace", "6", Role.GUEST, Technicality.TECHNICAL);
//		dataSer.saveNewUser(grace);
	}

	// initializes a Repository with Rules for demonstration and testing in the
	// database
	public void initializeTestRuleRepository() {

		dataSer.deleteAllRules();

		// getting userIds from database to set as ownerIds for the rules
		String idAlice = dataSer.findUserByName("Alice").getUserId();
		String idBob = dataSer.findUserByName("Bob").getUserId();
		String idNoOwner = "0";

		ArrayList<LogEntry> triggers;
		ArrayList<String> conditions;
		ArrayList<LogEntry> actions;

		initiateDemoEntries();
		
		triggers = new ArrayList<LogEntry>();
		triggers.add(demoEntries.get(3));
		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(5));
		conditions = new ArrayList<String>();
		conditions.add("daily energy consumption is higher than the threshold");
		dataSer.saveNewRule(new Rule("rule 1 (coffee)", "1", demoEntries.get(4), triggers, conditions, actions, idAlice,
				"Rule_1: allows coffee to be made only until the daily energy consumption threshold is reached"));

		// initiateDemoEntries(2);
		triggers = new ArrayList<LogEntry>();
		triggers.add(demoEntries.get(7));
		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(9));
		conditions = new ArrayList<String>();
		conditions.add("a meeting in room 1 is going on");
//		actions.add("tv_mute null");

		dataSer.saveNewRule(new Rule("rule 2 (tv mute)", "2", demoEntries.get(8), triggers, conditions, actions, idBob,
				"Rule_2: mutes the TV if TV is playing while a meeting is going on"));

		// initiateDemoEntries(5);
		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(2));
		dataSer.saveNewError(new Error("Deebot error", "e1", actions, "the robotic vacuum cleaner is stuck",
				"remove barrier or set robot back on track"));
		

	}

	public void initializeTestOccurrenceRepository() {
		// TODO
	}

	void deleteAllOccurrencies() {
		dataSer.deleteAllOccurrencies();
	}

	// initiates the demoEntries-List with LogEntries for Demonstration and Testing
	public static void initiateDemoEntries() {
		demoEntries = new ArrayList<LogEntry>();
		ArrayList<String> other;

		// TODO name LogEntries in error case
		demoEntries.add(new LogEntry("2022-06-23T09:07:26.920189+00:00", "Deebot", "idle", "vacuum.deebot", null)); // 0
																													// scenario
																													// 5:
																													// index:
																													// 0
		demoEntries.add(new LogEntry("2022-06-23T09:07:26.932243+00:00", "Deebot", "error", "vacuum.deebot", null)); // 1
																														// scenario
																														// 5:
																														// index:
																														// 1

		other = new ArrayList<String>();
		other.add("icon\": \"mdi:alert-circle");
		demoEntries.add(new LogEntry("2022-06-23T09:07:26.933444+00:00", "Deebot last error", "104", // 2 scenario 5:
																										// index: 2
				"sensor.deebot_last_error", other));

		// trigger LogEntry
		demoEntries.add(
				new LogEntry("2022-06-23T09:50:50.014573+00:00", "state change", null, "scene.state_change", null)); // 3
																														// scenario
																														// 1:
																														// index:
																														// 0

		// rule LogEntry
		other = new ArrayList<String>();
		other.add("message\": \"triggered by state of sensor.smart_plug_social_room_coffee_today_s_consumption");
		other.add("source\": \"state of sensor.smart_plug_social_room_coffee_today_s_consumption");
		other.add("context_id\": \"01G67ZHDBKS302M9XP2GJTZAJH\", \"domain\": \"automation");
		demoEntries.add(new LogEntry("2022-06-23T09:50:50.229746+00:00", "sc1: Goal-Order-Conflict", null,
				"automation.test_scenario_watching_tv_light_off", other)); // 4 scenario 1: index: 1

		// action LogEntry
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
				"switch.smart_plug_social_room_coffee", other)); // 5 scenario 1: index: 2

		other = new ArrayList<String>();
		other.add("context_event_type\": \"automation_triggered");
		other.add("context_domain\": \"automation");
		other.add("context_name\": \"Welcome");
		other.add("context_message\": \"triggered by state of binary_sensor.door");
		other.add("context_source\": \"state of binary_sensor.door");
		other.add("context_entity_id\": \"automation.presence_notification");
		other.add("context_entity_id_name\": \"Welcome");
		demoEntries
				.add(new LogEntry("2022-06-23T11:19:30.181206+00:00", "Lab TV", "idle", "media_player.lab_tv", other)); // 6
																														// scenario
																														// 2:
																														// index:
																														// 0

		// trigger LogEntry
		demoEntries.add(
				new LogEntry("2022-06-23T11:19:31.024951+00:00", "Lab TV", "playing", "media_player.lab_tv", null)); // 7
																														// scenario
																														// 2:
																														// index:
																														// 1

		// rule LogEntry
		other = new ArrayList<String>();
		other.add("message\": \"triggered by state of media_player.lab_tv");
		other.add("source\": \"state of media_player.lab_tv");
		other.add("context_id\": \"01G684KSEJHD3DRWH9K36578E9");
		other.add("domain\": \"automation");
		other.add("context_state\": \"playing");
		other.add("context_entity_id\": \"media_player.lab_tv");
		other.add("context_entity_id_name\": \"Lab TV");
		demoEntries.add(new LogEntry("2022-06-23T11:19:31.028089+00:00", "sc2: Multi-User-Conflict", "null",
				"automation.sc2_multi_user_conflict", other)); // 8 scenario 2: index: 2

		// action LogEntry
		other = new ArrayList<String>();
		other.add("icon\": \"mdi:television");
		other.add("context_event_type\": \"automation_triggered");
		other.add("context_domain\": \"automation");
		other.add("context_name\": \"sc2: Multi-User-Conflict");
		other.add("context_message\": \"triggered by state of media_player.lab_tv");
		other.add("context_source\": \"state of media_player.lab_tv");
		other.add("context_entity_id\": \"automation.sc2_multi_user_conflict");
		other.add("context_entity_id_name\": \"sc2: Multi-User-Conflict");
		demoEntries.add(new LogEntry("2022-06-23T11:19:31.037231+00:00", "tv_mute", null, "scene.tv_playing", other)); // 9
																														// scenario
																														// 2:
																														// index:
																														// 3
		
	}
}
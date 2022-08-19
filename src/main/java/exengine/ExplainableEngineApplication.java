package exengine;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import exengine.database.DatabaseService;
import exengine.datamodel.*;
import exengine.haconnection.HomeAssistantConnectionService;

@SpringBootApplication
public class ExplainableEngineApplication implements CommandLineRunner {

	@Autowired
	private HomeAssistantConnectionService haService;

	@Autowired
	DatabaseService dataSer;
	
	public static ArrayList<LogEntry> demoEntries;

	public static void main(String[] args) {
		SpringApplication.run(ExplainableEngineApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		intializeTestUserRepository();
		initializeTestRuleRepository();
		haService.printAPIStatus();

//		System.out.println(ruleRepo.findRuleById("ObjectId('62f66091fc241a67fa73fc4c')").get(0).ruleName);
//		System.out.println(ruleRepo.findById("62fa0875a078fc18d5a07165").get().ruleName);
//
//		System.out.println(ruleRepo.findRuleByName("sc1: Goal-Order-Conflict null").get(0).ruleName);
//		System.out.println(ruleRepo.findRuleByName("sc1: Goal-Order-Conflict null").get(0).id);

	}
	
	public void intializeTestUserRepository() {
		dataSer.deleteAllUsers();
		
		User alice = new User("Alice", 1, Role.COWORKER, Technicality.TECHNICAL);
		dataSer.saveNewUser(alice);
		
		User bob = new User("Bob", 2, Role.COWORKER, Technicality.TECHNICAL);
		dataSer.saveNewUser(bob);
		
		User chuck = new User("Chuck", 3, Role.COWORKER, Technicality.TECHNICAL);
		dataSer.saveNewUser(chuck);
		
		User dana = new User("Dana", 4, Role.COWORKER, Technicality.MEDTECH);
		dataSer.saveNewUser(dana);
		
		User freyja = new User("Freyja", 5, Role.GUEST, Technicality.TECHNICAL);
		dataSer.saveNewUser(freyja);
		
		User grace = new User("Grace", 5, Role.GUEST, Technicality.TECHNICAL);
		dataSer.saveNewUser(grace);
	}

	public void initializeTestRuleRepository() {

		dataSer.deleteAllRules();

		int idAlice = 1;
		int idBob = 2;

		ArrayList<LogEntry> triggers;
		ArrayList<String> conditions;
		ArrayList<LogEntry> actions;

		initiateDemoEntries(1);
		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(1));		
		conditions = new ArrayList<String>();
		conditions.add("daily energy consumption is bigger than the set threshold");
		dataSer.saveNewRule(new Rule("rule 1 (coffee)", 1, demoEntries.get(0), null, conditions, actions, idAlice, "rule1 description", false));

		//(String ruleName, int ruleId, LogEntry ruleEntry, ArrayList<LogEntry> trigger, ArrayList<String> conditions, ArrayList<LogEntry> actions, int ownerId, String ruleDescription, boolean isError)

		initiateDemoEntries(2);
		triggers = new ArrayList<LogEntry>();
		triggers.add(demoEntries.get(1));
		actions = new ArrayList<LogEntry>();
		actions.add(demoEntries.get(3));
		conditions = new ArrayList<String>();
		conditions.add("meeting going on");
//		actions.add("tv_mute null");
		
		dataSer.saveNewRule(new Rule("rule 2 (tv mute)", 2, demoEntries.get(2), triggers, conditions, actions, idBob, "rule2 description", false));

//		triggers = new ArrayList<String>();
//		triggers.add("Deebot idle");
//
//		conditions = new ArrayList<String>();
		/*
		 * Do we need a condition here? There is no rule
		 */
//		conditions.add("Deebot running");
//		actions = new ArrayList<String>();
//		actions.add("Deebot last error 104");
//		dataSer.saveNewRule(new Rule("Deebot error", triggers, conditions, actions, idLars, "error", true));
	}
	
	public static void initiateDemoEntries(int scenario) {
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
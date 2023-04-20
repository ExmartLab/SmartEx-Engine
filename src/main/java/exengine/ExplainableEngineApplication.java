package exengine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import exengine.database.DatabaseService;
import exengine.datamodel.*;
import exengine.datamodel.Error;
import exengine.haconnection.HomeAssistantConnectionService;
import exengine.loader.JsonHandler;

@SpringBootApplication
public class ExplainableEngineApplication implements CommandLineRunner {
	
	public final static String FILE_NAME_USERS = "seeds/users.yaml";
	public final static String FILE_NAME_ENTITIES = "seeds/entities.yaml";
	public final static String FILE_NAME_DEMO_LOGS = "demoLogs.json";

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
			
			try {
				populateDemoEntries(FILE_NAME_DEMO_LOGS);
				System.out.println("2: " + demoEntries.get(2));
				System.out.println("3: " + demoEntries.get(3));
				System.out.println("4: " + demoEntries.get(4));
				System.out.println("5: " + demoEntries.get(5));
				System.out.println("7: " + demoEntries.get(7));
				System.out.println("8: " + demoEntries.get(8));
				System.out.println("9: " + demoEntries.get(9));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			initializeTestRuleRepository();
			
		}

		// print out current API Status to see that HA is reachable
		haService.printAPIStatus();
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
	
	public static void populateDemoEntries(String fileName) throws IOException, URISyntaxException {
		String logJSON = JsonHandler.loadFile(fileName);
		demoEntries = JsonHandler.loadLogEntriesFromJson(logJSON);
	}

}
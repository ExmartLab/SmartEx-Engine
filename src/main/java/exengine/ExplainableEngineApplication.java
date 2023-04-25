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
	public final static String FILE_NAME_RULES = "seeds/rules.yaml";
	public final static String FILE_NAME_ERRORS = "seeds/errors.yaml";
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
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			//initializeTestRuleRepository();
			
		}

		// print out current API Status to see that HA is reachable
		haService.printAPIStatus();
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
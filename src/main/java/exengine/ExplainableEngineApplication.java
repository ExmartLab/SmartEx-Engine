package exengine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import exengine.datamodel.LogEntry;
import exengine.haconnection.HomeAssistantConnectionService;
import exengine.loader.JsonHandler;

@SpringBootApplication
public class ExplainableEngineApplication implements CommandLineRunner {
	
	private static final Logger logger = LoggerFactory.getLogger(ExplainableEngineApplication.class);
	
	public static final String FILE_NAME_USERS = "seeds/users.yaml";
	public static final String FILE_NAME_ENTITIES = "seeds/entities.yaml";
	public static final String FILE_NAME_RULES = "seeds/rules.yaml";
	public static final String FILE_NAME_ERRORS = "seeds/errors.yaml";
	public static final String FILE_NAME_DEMO_LOGS = "demoLogs.json";

	@Autowired
	private HomeAssistantConnectionService haService;

	private static boolean debug = true;
	private static boolean testing = true;

	public static ArrayList<LogEntry> demoEntries;

	public static void main(String[] args) {
		SpringApplication.run(ExplainableEngineApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if (testing) {
			try {
				populateDemoEntries();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// print out current API Status to see that HA is reachable
		haService.printAPIStatus();
	}
	
	public static void populateDemoEntries() throws IOException, URISyntaxException {
		String logJSON = JsonHandler.loadFile(FILE_NAME_DEMO_LOGS);
		demoEntries = JsonHandler.loadLogEntriesFromJson(logJSON);
		logger.info("demoEntries have been loaded from " + FILE_NAME_DEMO_LOGS);
	}

	public static boolean isDebug() {
		return debug;
	}

	public static void setDebug(boolean debug) {
		ExplainableEngineApplication.debug = debug;
	}
	
	public static boolean isTesting() {
		return testing;
	}

	public static void setTesting(boolean testing) {
		ExplainableEngineApplication.testing = testing;
	}

}
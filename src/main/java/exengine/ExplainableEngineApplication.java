package exengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExplainableEngineApplication implements CommandLineRunner {
	
	private static final Logger logger = LoggerFactory.getLogger(ExplainableEngineApplication.class);
	
	public static final String FILE_NAME_USERS = "seeds/users.yaml";
	public static final String FILE_NAME_ENTITIES = "seeds/entities.yaml";
	public static final String FILE_NAME_RULES = "seeds/rules.yaml";
	public static final String FILE_NAME_ERRORS = "seeds/errors.yaml";
	public static final String FILE_NAME_DEMO_LOGS = "demoLogs.json";

	private static boolean testing = true;

	public static void main(String[] args) {
		SpringApplication.run(ExplainableEngineApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception { }
	
	public static boolean isTesting() {
		return testing;
	}

	public static void setTesting(boolean testing) {
		ExplainableEngineApplication.testing = testing;
		logger.debug("Testing set to {}", testing);
	}

}
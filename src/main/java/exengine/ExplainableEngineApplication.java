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

	private static boolean demo = true;

	public static void main(String[] args) {
		SpringApplication.run(ExplainableEngineApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		logger.info("ExplainableEngineApplication running");
	}
	
	public static boolean isDemo() {
		return demo;
	}

	public static void setDemo(boolean demo) {
		ExplainableEngineApplication.demo = demo;
		logger.debug("Demo mode set to {}", demo);
	}

}
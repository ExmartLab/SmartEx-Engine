package exengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ExplainableEngineApplication: A prototype of the SmartEx reference
 * architecture for generating user-centric explanations in smart environments
 * (Here: the smart home managing system "Home Assistant").
 * 
 * Entry point for the application.
 * 
 * Also defines the global {@link #demo demo} variable, as well as constants
 * defining all file-names that are accessed within the application.
 */
@SpringBootApplication
public class ExplainableEngineApplication implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExplainableEngineApplication.class);

	public static final String FILE_NAME_USERS = "seeds/users.yaml";
	public static final String FILE_NAME_ENTITIES = "seeds/entities.yaml";
	public static final String FILE_NAME_RULES = "seeds/rules.yaml";
	public static final String FILE_NAME_ERRORS = "seeds/errors.yaml";
	public static final String FILE_NAME_DEMO_LOGS = "demoLogs.json";
	public static final String FILE_NAME_DEMO_LOGS_1 = "demoLogsContrastive1.json";
	public static final String FILE_NAME_DEMO_LOGS_2 = "demoLogsContrastive2.json";

	/**
	 * Configuration property of the application.
	 * 
	 * If <code>demo=true</code>, explanations will be generated based on a
	 * predefined and fixed set of logs (i.e., historical and constructed events in
	 * Home Assistant). Else, the explanations will be generated based on an
	 * up-to-date fetch of logs (i.e., most recent/live events in Home Assistant).
	 * 
	 * the <code>demoScenario</code> states which events for demonstration shall be
	 * used if <code>demo=true</code>
	 * 0: default Causal explanation
	 * 1: contrastive scenario 1
	 * 2: contrastive scenario 2
	 * 
	 * @Note If <code>demo=false</code>, the application needs to have a connection
	 *       to Home Assistant.
	 */
	private static boolean demo = true;
	private static int demoScenario = 1;

	/** Entry Point of this applciation. */
	public static void main(String[] args) {
		SpringApplication.run(ExplainableEngineApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		LOGGER.info("Explainable Engine running");
	}

	/**
	 * Gets the demo mode configuration.
	 * 
	 * @return boolean {@link #demo demo}
	 */
	public static boolean isDemo() {
		return demo;
	}

	/**
	 * Sets the demo mode configuration.
	 * 
	 * @param demo A boolean value indicating whether the application should run in
	 *             demo mode. Set to 'true' for enabling demo mode, 'false'
	 *             otherwise.
	 * @see {@link #demo demo mode} for implications.
	 */
	public static void setDemo(boolean demo) {
		ExplainableEngineApplication.demo = demo;
		LOGGER.debug("Demo mode set to {}", demo);
	}

	/**
	 * Gets the demo scenario.
	 * 
	 * @return demo scenario
	 */
	public static int getDemoScenario() {
		return demoScenario;
	}

	/**
	 * Sets the demo scenario configuration.
	 * 
	 * @param demoScenario an int that sets the scenario for demonstration
	 * 
	 * @see {@link #demo demo mode} for implications.
	 */
	public static void setDemoScenario(int demoScenario) {
		ExplainableEngineApplication.demoScenario = demoScenario;
		LOGGER.debug("Demo scenario set to {}", demoScenario);
	}

}
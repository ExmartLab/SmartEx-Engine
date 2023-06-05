package exengine.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.qos.logback.classic.Level;
import exengine.ExplainableEngineApplication;

/**
 * REST controller for all live (runtime) configurations made to the
 * ExplainableEngineApplication.
 */
@RestController
public class ConfigurationController {

	private static final Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

	/**
	 * Retrieves the status of the Explainable Engine.
	 *
	 * @return A ResponseEntity containing a String message indicating the status and HttpStatus.OK.
	 */
	@GetMapping("/status")
	public ResponseEntity<String> getStatus() {
		logger.info("HTTP GET: Status returned");
		return new ResponseEntity<>("Explainable Engine running", HttpStatus.OK);
	}

	/**
	 * Turns off the testing mode for the Explainable Engine.
	 *
	 * @return A ResponseEntity containing a String message indicating that testing has been turned off and HttpStatus.CREATED.
	 */
	@PostMapping("/demo/off")
	public ResponseEntity<String> demoOff() {
		ExplainableEngineApplication.setDemo(false);
		logger.info("HTTP POST: Testing turned off");
		return new ResponseEntity<>("Testing turned off", HttpStatus.CREATED);
	}

	/**
	 * Turns on the testing mode for the Explainable Engine.
	 *
	 * @return A ResponseEntity containing a String message indicating that testing has been turned on and HttpStatus.CREATED.
	 */
	@PostMapping("/demo/on")
	public ResponseEntity<String> demoOn() {
		ExplainableEngineApplication.setDemo(true);
		logger.info("HTTP POST: Testing turned on");
		return new ResponseEntity<>("Testing turned on", HttpStatus.CREATED);
	}

	/**
	 * Retrieves the current log level of the Explainable Engine.
	 *
	 * @return A ResponseEntity containing a String message indicating the current log level and HttpStatus.OK.
	 */
	@GetMapping("/log-level/get")
	public ResponseEntity<String> getLogLevel() {
		ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		Level level = rootLogger.getLevel();
		logger.debug("HTTP GET: Log level was requested, which is {}", level);
		return new ResponseEntity<>(String.format("The current log level is %s", level), HttpStatus.OK);
	}

	/**
	 * Sets the log level of the Explainable Engine.
	 *
	 * @param levelString The desired log level as a String. Valid values are "error", "warn", "info", "debug", and "trace".
	 *                    Defaults to "INFO" if no valid log level is provided.
	 * @return A ResponseEntity containing a String message indicating the new log level and HttpStatus.OK if successful,
	 *         or a String message indicating an invalid log level and HttpStatus.BAD_REQUEST.
	 */
	@PostMapping("/log-level/set")
	public ResponseEntity<String> setLogLevel(
			@RequestParam(value = "level", defaultValue = "INFO") String levelString) {

		Level newLevel;

		switch (levelString.toLowerCase()) {
		case "error":
			newLevel = Level.ERROR;
			break;
		case "warn":
			newLevel = Level.WARN;
			break;
		case "info":
			newLevel = Level.INFO;
			break;
		case "debug":
			newLevel = Level.DEBUG;
			break;
		case "trace":
			newLevel = Level.TRACE;
			break;
		default:
			return new ResponseEntity<>(
					"The provided log level does not match any of the following: error, warn, info, debug, trace.",
					HttpStatus.BAD_REQUEST);
		}

		ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		rootLogger.setLevel(newLevel);

		logger.info("Log level changed to {}", rootLogger.getLevel());

		return new ResponseEntity<>(String.format("Log level changed to %s", newLevel), HttpStatus.OK);
	}

}

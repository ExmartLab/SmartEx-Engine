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

@RestController
public class ConfigurationController {

	private static final Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

	@GetMapping("/status")
	public ResponseEntity<String> getStatus() {
		logger.info("HTTP GET: Status returned");
		return new ResponseEntity<>("Explainable Engine running", HttpStatus.OK);
	}

	@PostMapping("/testing/off")
	public ResponseEntity<String> testingOff() {
		ExplainableEngineApplication.setTesting(false);
		logger.info("HTTP POST: Testing turned off");
		return new ResponseEntity<>("Testing turned off", HttpStatus.CREATED);
	}

	@PostMapping("/testing/on")
	public ResponseEntity<String> testingOn() {
		ExplainableEngineApplication.setTesting(true);
		logger.info("HTTP POST: Testing turned on");
		return new ResponseEntity<>("Testing turned on", HttpStatus.CREATED);
	}

	@GetMapping("/log-level/get")
	public ResponseEntity<String> getLogLevel() {
		ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		Level level = rootLogger.getLevel();
		logger.info("Log level is {}", level);
		return new ResponseEntity<>(String.format("The current log level is %s", level), HttpStatus.OK);
	}

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

		return new ResponseEntity<>(String.format("Log level changed to %s", newLevel), HttpStatus.OK);
	}

}

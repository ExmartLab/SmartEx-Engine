package exengine.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import exengine.ExplainableEngineApplication;

@RestController
public class ConfigurationController {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

	@GetMapping("/status")
	public ResponseEntity<String> getStatus() {
		logger.info("HTTP GET: Status returned");
		return new ResponseEntity<>("Explainable Engine running", HttpStatus.OK);
	}

	@PostMapping("/testingoff")
	public ResponseEntity<String> testingOff() {
		ExplainableEngineApplication.setTesting(false);
		logger.info("HTTP POST: Testing turned off");
		return new ResponseEntity<>("Testing turned off", HttpStatus.CREATED);
	}

	@PostMapping("/testingon")
	public ResponseEntity<String> testingOn() {
		ExplainableEngineApplication.setTesting(true);
		logger.info("HTTP POST: Testing turned on");
		return new ResponseEntity<>("Testing turned on", HttpStatus.CREATED);
	}

}

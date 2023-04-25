package exengine.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import exengine.ExplainableEngineApplication;

@RestController
public class ConfigurationController {

	@GetMapping("/status")
	public ResponseEntity<String> getStatus() {
		if (ExplainableEngineApplication.isDebug())
			System.out.println("HTTP GET: Status returned");
		return new ResponseEntity<>("Explainable Engine running", HttpStatus.OK);
	}

	@PostMapping("/debugoff")
	public ResponseEntity<String> debugOff() {
		if (ExplainableEngineApplication.isDebug())
			System.out.println("HTTP POST: Debugging turned off");
		ExplainableEngineApplication.setDebug(false);
		return new ResponseEntity<>("Debugging turned off", HttpStatus.CREATED);
	}

	@PostMapping("/debugon")
	public ResponseEntity<String> debugOn() {
		System.out.println("HTTP POST: Debugging turned on");
		ExplainableEngineApplication.setDebug(true);
		return new ResponseEntity<>("Debugging turned on", HttpStatus.CREATED);
	}

	@PostMapping("/testingoff")
	public ResponseEntity<String> testingOff() {
		if (ExplainableEngineApplication.isDebug())
			System.out.println("HTTP POST: Testing turned off");
		ExplainableEngineApplication.setTesting(false);
		return new ResponseEntity<>("Testing turned off", HttpStatus.CREATED);
	}

	@PostMapping("/testingon")
	public ResponseEntity<String> testingOn() {
		System.out.println("HTTP POST: Testing turned on");
		ExplainableEngineApplication.setTesting(true);
		return new ResponseEntity<>("Testing turned on", HttpStatus.CREATED);
	}

}

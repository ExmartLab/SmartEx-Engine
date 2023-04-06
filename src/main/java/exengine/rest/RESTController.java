package exengine.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import exengine.ExplainableEngineApplication;
import exengine.engineService.CreateExService;

//@RequestMapping("/ExEngine")
@RestController
public class RESTController {

	@Autowired
	CreateExService createExSer;

	// returns a status if the explainable engine is running
	@GetMapping("/status")
	public ResponseEntity<String> getStatus() {
		if (ExplainableEngineApplication.debug)
			System.out.println("HTTP GET: Status returned");
		return new ResponseEntity<>("Explainable Engine running", HttpStatus.OK);
	}

	// returns the explanation as a String by calling the createExplanationService
	@GetMapping("/explain")
	public ResponseEntity<String> getExplanation(@RequestParam(value = "min", defaultValue = "30") String min,
			@RequestParam(value = "userid", defaultValue = "0") String userId,
			@RequestParam(value = "userState", defaultValue = "unknown") String userState,
			@RequestParam(value = "userLocation", defaultValue = "unknown") String userLocation,
			@RequestParam(value = "device", defaultValue = "unknown") String device) {

		// initiating integer variables
		int minNumber = 30;

		// trying to assign the given values to the integer variables
		try {
			minNumber = Integer.parseInt(min);
		} catch (Exception e) {
		}
		if (ExplainableEngineApplication.debug)
			System.out.println("HTTP GET: Explanation requested (last " + minNumber + " min), userId: " + userId);
		String explanation = createExSer.getExplanation(minNumber, userId, userState, userLocation, device);
		return new ResponseEntity<>(explanation, HttpStatus.OK);
	}

	@GetMapping("/show")
	public ResponseEntity<String> runShowCases(
			@RequestParam(value = "scenarioid", defaultValue = "0") String scenarioId,
			@RequestParam(value = "userid", defaultValue = "0") String userId,
			@RequestParam(value = "userState", defaultValue = "unknown") String userState,
			@RequestParam(value = "userLocation", defaultValue = "unknown") String userLocation,
			@RequestParam(value = "device", defaultValue = "unknown") String device) {

		// initiating integer variables
		int minNumber = 30;
		int scenarioNumber = 0;

		// trying to assign the given values to the integer variables
		try {
			scenarioNumber = Integer.parseInt(scenarioId);
		} catch (Exception e) {
		}
		if (ExplainableEngineApplication.debug)
			System.out.println("HTTP GET: Showcase: (last " + minNumber + " min), userId: " + userId);
		ExplainableEngineApplication.testingScenario = scenarioNumber;
		ExplainableEngineApplication.initiateDemoEntries(scenarioNumber);
		ExplainableEngineApplication.testing = true;
		String explanation = createExSer.getExplanation(minNumber, userId, userState, userLocation, device);
		
		// turn testing off again
		ExplainableEngineApplication.testing = false;
		return new ResponseEntity<>(explanation, HttpStatus.OK);
	}

	@PostMapping("/debugoff")
	public ResponseEntity<String> debugOff() {
		if (ExplainableEngineApplication.debug)
			System.out.println("HTTP POST: Debugging turned off");
		ExplainableEngineApplication.debug = false;
		return new ResponseEntity<>("Debugging turned off", HttpStatus.CREATED);
	}

	@PostMapping("/debugon")
	public ResponseEntity<String> debugOn() {
		System.out.println("HTTP POST: Debugging turned on");
		ExplainableEngineApplication.debug = true;
		return new ResponseEntity<>("Debugging turned on", HttpStatus.CREATED);
	}

	@PostMapping("/testingoff")
	public ResponseEntity<String> testingOff() {
		if (ExplainableEngineApplication.debug)
			System.out.println("HTTP POST: Testing turned off");
		ExplainableEngineApplication.testing = false;
		return new ResponseEntity<>("Testing turned off", HttpStatus.CREATED);
	}

	@PostMapping("/testingon")
	public ResponseEntity<String> testingOn() {
		System.out.println("HTTP POST: Testing turned on");
		ExplainableEngineApplication.testing = true;
		return new ResponseEntity<>("Testing turned on", HttpStatus.CREATED);
	}

	@GetMapping("/greeting")
	public ResponseEntity<String> greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		if (ExplainableEngineApplication.debug)
			System.out.println("HTTP GET: Greeting for " + name + " requested");
		return new ResponseEntity<>("Hello " + name + "!", HttpStatus.OK);
	}

}

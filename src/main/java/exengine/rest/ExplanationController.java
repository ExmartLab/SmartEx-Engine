package exengine.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import exengine.ExplainableEngineApplication;
import exengine.engineService.CreateExService;

@RestController
public class ExplanationController {
	
	private static final Logger logger = LoggerFactory.getLogger(ExplanationController.class);

	@Autowired
	CreateExService createExSer;

	// returns the explanation as a String by calling the createExplanationService
	@GetMapping("/explain")
	public ResponseEntity<String> getExplanation(@RequestParam(value = "min", defaultValue = "30") String min,
			@RequestParam(value = "userid", defaultValue = "0") String userId,
			@RequestParam(value = "device", defaultValue = "unknown") String device) {

		// initiating integer variables
		int minNumber = 30;

		// trying to assign the given values to the integer variables
		try {
			minNumber = Integer.parseInt(min);
		} catch (Exception e) {
		}
		
		logger.debug("HTTP GET: Explanation requested: last {} min, userId: {}, device: {}", minNumber, userId, device);
		
		String explanation = createExSer.getExplanation(minNumber, userId, device);
		return new ResponseEntity<>(explanation, HttpStatus.OK);
	}

	@GetMapping("/show")
	public ResponseEntity<String> runShowCases(@RequestParam(value = "userid", defaultValue = "0") String userId,
			@RequestParam(value = "userLocation", defaultValue = "unknown") String userLocation,
			@RequestParam(value = "device", defaultValue = "unknown") String device) {

		// initiating integer variables
		int minNumber = 30;
		
		logger.debug("HTTP GET: Showcase: (last {} min, per default), userId: {}, userLocation: {}, device: {}", minNumber, userId, userLocation, device);

		ExplainableEngineApplication.setDemo(true);
		String explanation = createExSer.getExplanation(minNumber, userId, device);

		// turn testing off again
		ExplainableEngineApplication.setDemo(false);
		return new ResponseEntity<>(explanation, HttpStatus.OK);
	}

}

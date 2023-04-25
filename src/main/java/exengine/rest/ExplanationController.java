package exengine.rest;

import java.io.IOException;
import java.net.URISyntaxException;

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

	@Autowired
	CreateExService createExSer;

	// returns the explanation as a String by calling the createExplanationService
	@GetMapping("/explain")
	public ResponseEntity<String> getExplanation(@RequestParam(value = "min", defaultValue = "30") String min,
			@RequestParam(value = "userid", defaultValue = "0") String userId,
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
		String explanation = createExSer.getExplanation(minNumber, userId, userLocation, device);
		return new ResponseEntity<>(explanation, HttpStatus.OK);
	}

	@GetMapping("/show")
	public ResponseEntity<String> runShowCases(@RequestParam(value = "userid", defaultValue = "0") String userId,
			@RequestParam(value = "userLocation", defaultValue = "unknown") String userLocation,
			@RequestParam(value = "device", defaultValue = "unknown") String device) {

		// initiating integer variables
		int minNumber = 30;

		if (ExplainableEngineApplication.debug)
			System.out.println("HTTP GET: Showcase: (last " + minNumber + " min), userId: " + userId);
		try {
			try {
				ExplainableEngineApplication.populateDemoEntries();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ExplainableEngineApplication.testing = true;
		String explanation = createExSer.getExplanation(minNumber, userId, userLocation, device);

		// turn testing off again
		ExplainableEngineApplication.testing = false;
		return new ResponseEntity<>(explanation, HttpStatus.OK);
	}

}

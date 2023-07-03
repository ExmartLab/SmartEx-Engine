package exengine.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import exengine.engineservice.CausalExplanationService;

/**
 * REST controller for providing explanation demanding endpoints.
 */
@RestController
public class ExplanationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExplanationController.class);

	@Autowired
	CausalExplanationService createExSer;

	/**
	 * Generates and returns an explanation for the provided attributes.
	 * 
	 * @param min    number of minutes in history to take into account
	 * @param userId the user identifier of the explainee
	 * @param device the device which exhibited the explanandum
	 * @return A ResponseEntity containing a String message containing the
	 *         explanation and HttpStatus.OK.
	 */
	@GetMapping("/explain")
	public ResponseEntity<String> getExplanation(@RequestParam(value = "min", defaultValue = "30") String min,
			@RequestParam(value = "userid", defaultValue = "0") String userId,
			@RequestParam(value = "device", defaultValue = "unknown") String device) {

		int minNumber = 30;

		try {
			minNumber = Integer.parseInt(min);
		} catch (NumberFormatException e) {
			LOGGER.error(e.getMessage());
		}

		LOGGER.debug("HTTP GET: Explanation requested: last {} min, userId: {}, device: {}", minNumber, userId, device);

		String explanation = createExSer.getExplanation(minNumber, userId, device);
		return new ResponseEntity<>(explanation, HttpStatus.OK);
	}

}

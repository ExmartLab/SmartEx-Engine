package exengine.rest;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import exengine.database.DatabaseService;
import exengine.datamodel.State;
import exengine.datamodel.User;

/**
 * REST controller for all live (runtime) configurations made to the
 * ExplainableEngineApplication's database.
 */
@RestController
@RequestMapping("/database")
public class DatabaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseController.class);

	@Autowired
	DatabaseService dataSer;

	/**
	 * Sets the state of a user and updates the database accordingly.
	 * 
	 * @param userId    the user identifier of the user whose state is to be changed
	 * @param userState the new state of the user
	 * @return A ResponseEntity containing a String message indicating how the state
	 *         has changed and HttpStatus.OK if the transaction was successful.
	 */
	@PostMapping("state")
	public ResponseEntity<String> setUserState(@RequestParam(value = "userid", defaultValue = "0") String userId,
			@RequestParam(value = "userState", defaultValue = "unknown") String userState) {

		Optional<User> optionalUser = Optional.ofNullable(dataSer.findUserByUserId(userId));
		if (optionalUser.isPresent()) {

			State state;
			if (userState.equals(State.WORKING.getString())) {
				state = State.WORKING;
			} else if (userState.equals(State.MEETING.getString())) {
				state = State.MEETING;
			} else if (userState.equals(State.BREAK.getString())) {
				state = State.BREAK;
			} else {
				return new ResponseEntity<>(
						"userState does not match any of the following: \"working\", \"break\", or \"meeting\".",
						HttpStatus.BAD_REQUEST);
			}

			User user = optionalUser.get();
			String oldState = user.getStateString();
			user.setState(state);
			dataSer.saveNewUser(user);

			LOGGER.info("HTTP Post: User {} changed state from {} to {}", user.getName(), oldState, state);
			return new ResponseEntity<>(
					String.format("User %s (id: %s) changed state to \"%s\"", user.getName(), userId, state),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
		}
	}

}

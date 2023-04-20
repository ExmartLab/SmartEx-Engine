package exengine.rest;

import java.util.Optional;

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

@RestController
@RequestMapping("/users")
public class DatabaseController {

	@Autowired
	DatabaseService dataSer;
	
	@PostMapping("state")
	public ResponseEntity<String> setUserState(
			@RequestParam(value = "userid", defaultValue = "0") String userId,
			@RequestParam(value = "userState", defaultValue = "unknown") String userState) {
		
		Optional<User> optionalUser = Optional.ofNullable(dataSer.findUserByUserId(userId));
        if (optionalUser.isPresent()) {
            
            State state = State.BREAK;
    		if (userState.equals(State.WORKING.toString()))
    			state = State.WORKING;
    		else if (userState.equals(State.MEETING.toString()))
    			state = State.MEETING;
    		
    		User user = optionalUser.get();
			user.setState(state);
            dataSer.saveNewUser(user);
            return new ResponseEntity<>(String.format("User %s (id: %s) changed state to \"%s\"", user.getName(), userId, userState), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
	} 

}

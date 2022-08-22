package exengine.createexplanation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.ExplainableEngineApplication;
import exengine.contextmanagement.ContextService;
import exengine.database.*;
import exengine.datamodel.*;
import exengine.explanationtypes.*;
import exengine.rulebook.ExplanationTypeService;

@Service
public class CreateExService {

	private ArrayList<LogEntry> logEntries;

	@Autowired
	DatabaseService dataSer;
	
	@Autowired
	FindCauseService findCauseSer;
	
	@Autowired
	ContextService conSer;
	
	@Autowired
	ExplanationTypeService exTypeSer;
	
	@Autowired
	ExplanationGenerationService exGenSer;
	
	private boolean debug = true;

	public String getExplanation(int min, String userId, String userState, String userLocation) {

		/*
		 * test for valid userId by checking if user with userId is in db
		 */
//		User user = dataSer.findUserByUserId(userId);
//		if(user == null)
//			return "unvalid userId";
		

//		User user = dataSer.findUserByName("Bob");
//		if(user == null)
//			return "unvalid userId";

//		if(debug)
//			System.out.println("found user: " + user.getName());

		
		User user2 = dataSer.findUserByUserId(userId);
		if(user2 == null)
			return "unvalid userId";

		if(debug)
			System.out.println("found user: " + user2.getName());
			
		// turned off for testing
//		logEntries = HA_API.parseLastLogs(min);

		// only for testing
		int scenario = 2;
		System.out.println("Demo for Scenario " + scenario);
		ExplainableEngineApplication.initiateDemoEntries(scenario);
		logEntries = ExplainableEngineApplication.demoEntries;

		// default value for return string
		String explanation = "no explanation found";

		// query Rules from DB
		List<Rule> dbRules = dataSer.findAllRules();

		/*
		 * STEP 1: FIND CAUSE
		 */
		Cause cause = findCauseSer.findCause(logEntries, dbRules);

		// return in case no cause has been found
		if (cause == null)
			return "couldn't find cause to explain";

		/*
		 * STEP 2: TODO get context (method in class)
		 */
		//default state break
		State state = State.BREAK;
		//test for other cases
		if(userState.equals(State.WORKING.toString()))
			state = State.WORKING;
		else if(userState.equals(State.MEETING.toString()))
			state = State.MEETING;

		//turned off until occurrence is working
		Context context = conSer.getAllContext(cause, userId, state, userLocation);
		
		// for testing
		//Context context = new Context(Role.GUEST, Occurrence.SECOND, Technicality.MEDTECH, State.WORKING, null);

		/*
		 * STEP 3: ask rule engine what explanation type to generate
		 */
		ExplanationType type = exTypeSer.getExplanationType(context);

		//TESTING
		type = ExplanationType.FULLEX;
		
		/*
		 * STEP 4: TODO generate the desired explanation (methods in classes) MORE
		 * CONTEXT NEEDED
		 */
		if (type == null)
			return "no explanation for given context";

		switch (type) {
		case FULLEX:
			explanation = exGenSer.getFullExplanation(cause, context);
			break;
		case RULEEX:
			explanation = exGenSer.getRuleExplanation(cause, context);
			break;
		case FACTEX:
			explanation = exGenSer.getFactExplanation(cause, context);
			break;
		case SIMPLDEX:
			explanation = exGenSer.getSimplifiedExplanation(cause, context);
			break;
		}

		return explanation;
	}

	/*
	 * public List<Rule> findRulesByName() { return
	 * ruleRepo.findByRuleName("testRule1"); }
	 */

}
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

	public String getExplanation(int min, String userId, String userState, String userLocation) {

		// test for valid userId by checking if user with userId is in db
		User user = dataSer.findUserByUserId(userId);
		if (user == null)
			return "unvalid userId";

		if (ExplainableEngineApplication.debug)
			System.out.println("found user: " + user.getName());

		// turned off for testing
//		logEntries = HA_API.parseLastLogs(min);

		// only for testing
		int scenario = 2;
		if (ExplainableEngineApplication.debug)
			System.out.println("Demo for Scenario " + scenario);
		ExplainableEngineApplication.initiateDemoEntries(scenario);
		logEntries = ExplainableEngineApplication.demoEntries;

		// default value for return string
		String explanation = "no explanation found";

		// query Rules from DB
		List<Rule> dbRules = dataSer.findAllRules();

		if (ExplainableEngineApplication.debug)
			System.out.println("\n------ EXPLANATION ALGORITHM ------");

		/*
		 * STEP 1: FIND CAUSE
		 */
		Cause cause = findCauseSer.findCause(logEntries, dbRules);

		// return in case no cause has been found
		if (cause == null)
			return "couldn't find cause to explain";

		if (ExplainableEngineApplication.debug) {
			System.out.println("\nCause:");
			System.out.println("ruleId: " + cause.getRule().ruleId);
			System.out.println("trigger: " + cause.getTriggerString());
			System.out.println("conditions: " + cause.getConditionsString());
			System.out.println("actions: " + cause.getActionsString());
		}

		/*
		 * STEP 2: GET CONTEXT
		 */
		// default state break
		State state = State.BREAK;

		// test for other cases
		if (userState.equals(State.WORKING.toString()))
			state = State.WORKING;
		else if (userState.equals(State.MEETING.toString()))
			state = State.MEETING;

		Context context = conSer.getAllContext(cause, userId, state, userLocation);

		// for testing
		// Context context = new Context(Role.GUEST, Occurrence.SECOND,
		// Technicality.MEDTECH, State.WORKING, null);

		if (ExplainableEngineApplication.debug) {
			System.out.println("\nContext:");
			System.out.println("owner: " + context.getOwnerName());
			System.out.println("rule: " + context.getRuleDescription());
			System.out.println("explainee: " + context.getExplaineeName());
			System.out.println("role: " + context.getExplaineeRole().toString());
			System.out.println("state: " + context.getExplaineeState().toString());
			System.out.println("technicality: " + context.getExplaineeTechnicality().toString());
			System.out.println("occurrence: " + context.getOccurrence());
		}

		/*
		 * STEP 3: ask rule engine what explanation type to generate
		 */
		ExplanationType type = exTypeSer.getExplanationType(context);

		/*
		 * STEP 4: generate the desired explanation
		 */
		if (type == null)
			return "coudln't determine explanation type";

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

		if (ExplainableEngineApplication.debug) {
			System.out.println("\nFinalExplanation:");
			System.out.println(explanation);
			System.out.println("\n------ EXPLANATION CREATED ------\n");
		}

		return explanation;
	}

}
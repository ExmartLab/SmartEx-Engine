package exengine.engineService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.ExplainableEngineApplication;
import exengine.algorithmicExpGenerator.FindCauseService;
import exengine.contextAwareExpGenerator.ExplanationContextMappingService;
import exengine.contextManager.ContextService;
import exengine.database.*;
import exengine.datamodel.*;
import exengine.datamodel.Error;
import exengine.expPresentation.*;
import exengine.haconnection.HomeAssistantConnectionService;

@Service
public class CreateExService {

	private ArrayList<LogEntry> logEntries;

	@Autowired
	DatabaseService dataSer;

	@Autowired
	HomeAssistantConnectionService haSer;

	@Autowired
	FindCauseService findCauseSer;

	@Autowired
	ContextService conSer;

	@Autowired
	ExplanationContextMappingService contextMappingSer;

	@Autowired
	TransformationFunctionService transformFuncSer;

	public String getExplanation(int min, String userId, String userState, String userLocation) {

		// test for valid userId by checking if user with userId is in db
		User user = dataSer.findUserByUserId(userId);
		if (user == null)
			return "unvalid userId";
		if (ExplainableEngineApplication.debug)
			System.out.println("found user: " + user.getName());

		// getting the log Entries
		if (!ExplainableEngineApplication.testing) {
			// getting logs from Home Assistant
			logEntries = haSer.parseLastLogs(min);
		} else {
			// getting demo logs
			if (ExplainableEngineApplication.debug)
				System.out.println("Demo for Scenario " + ExplainableEngineApplication.testingScenario);
			ExplainableEngineApplication.initiateDemoEntries(ExplainableEngineApplication.testingScenario);
			logEntries = ExplainableEngineApplication.demoEntries;
		}

		// default value for return string
		String explanation = "no explanation found";

		// query Rules & Errors from DB
		List<Rule> dbRules = dataSer.findAllRules();
		List<Error> dbErrors = dataSer.findAllErrors();

		if (ExplainableEngineApplication.debug)
			System.out.println("\n------ EXPLANATION ALGORITHM ------");

		/*
		 * STEP 1: FIND CAUSE
		 */
		Cause cause = findCauseSer.findCause(logEntries, dbRules, dbErrors);
		// RuleCause cause = findCauseSer.findCause(logEntries, dbRules);

		// return in case no cause has been found
		if (cause == null)
			return "couldn't find cause to explain";

		//TODO debugging
//		if (ExplainableEngineApplication.debug) {
//			System.out.println("\nCause:");
//			System.out.println("ruleId: " + cause.getRule().ruleId);
//			System.out.println("trigger: " + cause.getTriggerString());
//			System.out.println("conditions: " + cause.getConditionsString());
//			System.out.println("actions: " + cause.getActionsString());
//		}

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

		// get final context from context service
		Context context = conSer.getAllContext(cause, userId, state, userLocation);

		// for testing
		// Context context = new Context(Role.GUEST, Occurrence.SECOND,
		// Technicality.MEDTECH, State.WORKING, null);

		if (ExplainableEngineApplication.debug) {
			System.out.println("\nContext:");
			System.out.println("owner: " + context.getOwnerName());
			System.out.println("explainee: " + context.getExplaineeName());
			System.out.println("role: " + context.getExplaineeRole().toString());
			System.out.println("state: " + context.getExplaineeState().toString());
			System.out.println("technicality: " + context.getExplaineeTechnicality().toString());
			System.out.println("occurrence: " + context.getOccurrence());
		}

		/*
		 * STEP 3: ask rule engine what explanation type to generate
		 */
		ExplanationType type = contextMappingSer.getExplanationType(context, cause);
		
		/*
		 * STEP 4: generate the desired explanation
		 */
		if (type == null)
			return "coudln't determine explanation type";

		if (ExplainableEngineApplication.debug)
			System.out.println("type: " + type.getValue());
		
		explanation = transformFuncSer.transformExplanation(type, cause, context);
		

		if (ExplainableEngineApplication.debug) {
			System.out.println("\nFinalExplanation:");
			System.out.println(explanation);
			System.out.println("\n------ EXPLANATION CREATED ------\n");
		}
		
		//display explanation in home assistant
//		haSer.postExplanation(explanation);
//		haSer.postExplanation("test");
		
		return explanation;
	}

}
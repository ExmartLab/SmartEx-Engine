package exengine.engineService;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private static final Logger logger = LoggerFactory.getLogger(CreateExService.class);

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

	public String getExplanation(int min, String userId, String userLocation, String device) {

		// test for valid userId by checking if user with userId is in db
		User user = dataSer.findUserByUserId(userId);
		if (user == null)
			return "unvalid userId";
		
		logger.debug("getExplanation called with arguments min: {}, user name: {}, userLocation: {}, device: {}", min, user.getName(), userLocation, device);
		
		// getting the log Entries
		if (!ExplainableEngineApplication.isTesting()) {
			// getting logs from Home Assistant
			try {
				logEntries = haSer.parseLastLogs(min);
			} catch (IOException e) {
				logger.error("Unable to parse last logs: {}", e.getMessage(), e);
			}
		} else {
			// getting demo logs
			try {
				ExplainableEngineApplication.populateDemoEntries();
			} catch (IOException | URISyntaxException e) {
				logger.error("Unable to populate the demo entries: {}", e.getMessage(), e);
			} 		
			
			logEntries = ExplainableEngineApplication.demoEntries;
		}
		
		ArrayList<String> entityIds = null;
		
		// check if explanation for particular device requested
		if (!device.equals("unknown")) {
			// get all associated entityIds
			entityIds = dataSer.findEntityIdsByDeviceName(device);
		}

		String explanation;

		// query Rules & Errors from DB
		List<Rule> dbRules = dataSer.findAllRules();
		List<Error> dbErrors = dataSer.findAllErrors();
		
		logger.debug("Explanation generation started");

		/*
		 * STEP 1: FIND CAUSE
		 */
		Cause cause = findCauseSer.findCause(logEntries, dbRules, dbErrors, entityIds);

		// return in case no cause has been found
		if (cause == null)
			return "couldn't find cause to explain";

		/*
		 * STEP 2: GET CONTEXT
		 */
		State state = user.getState();

		// get final context from context service
		Context context = conSer.getAllContext(cause, userId, state, userLocation);

		/*
		 * STEP 3: ask rule engine what explanation type to generate
		 */
		ExplanationType type = contextMappingSer.getExplanationType(context, cause);
		
		/*
		 * STEP 4: generate the desired explanation
		 */
		if (type == null)
			return "Unable to determine explanation type";
		
		explanation = transformFuncSer.transformExplanation(type, cause, context);
		
		logger.info("Explanation generated");
		
		return explanation;
	}

}
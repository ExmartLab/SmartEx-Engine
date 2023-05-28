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
import exengine.loader.JsonHandler;

@Service
public class CreateExService {

	private static final Logger logger = LoggerFactory.getLogger(CreateExService.class);

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

	public String getExplanation(int min, String userId, String device) {
	
		logger.debug("getExplanation called with arguments min: {}, user id: {}, device: {}", min, userId, device);

		List<Rule> dbRules = dataSer.findAllRules();
		List<Error> dbErrors = dataSer.findAllErrors();
		ArrayList<LogEntry> logEntries = getLogEntries(min);
		ArrayList<String> explanandumsEntityIds = getExplanandumsEntityIds(device);
		User user = dataSer.findUserByUserId(userId);

		if (user == null) {
			return "unvalid userId: this user does not exist";
		}

		// STEP 1: FIND CAUSE
		Cause cause = findCauseSer.findCause(logEntries, dbRules, dbErrors, explanandumsEntityIds);

		if (cause == null) {
			return "Could not find cause to explain";
		}

		// STEP 2: get final context from context service
		Context context = conSer.getAllContext(cause, user);

		if (context == null) {
			return "Could not collect context";
		}

		// STEP 3: ask rule engine what explanation type to generate
		View view = contextMappingSer.getExplanationView(context, cause);

		if (view == null) {
			return "Could not determine explanation type";
		}

		// STEP 4: generate the desired explanation
		String explanation = transformFuncSer.transformExplanation(view, cause, context);

		if (explanation == null) {
			return "Could not transform explanation into natural language";
		}

		logger.info("Explanation generated");
		return explanation;
	}

	public ArrayList<String> getExplanandumsEntityIds(String device) {
		if (device.equals("unknown")) {
			return new ArrayList<>();
		} else {
			return dataSer.findEntityIdsByDeviceName(device);
		}
	}

	public ArrayList<LogEntry> getLogEntries(int min) {
		ArrayList<LogEntry> logEntries = null;

		// getting the log Entries
		if (ExplainableEngineApplication.isDemo()) {
			// getting demo logs
			try {
				logEntries = populateDemoEntries(ExplainableEngineApplication.FILE_NAME_DEMO_LOGS);
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}

		} else {
			// getting logs from Home Assistant
			try {
				logEntries = haSer.parseLastLogs(min);
			} catch (IOException e) {
				logger.error("Unable to parse last logs: {}", e.getMessage(), e);
			}
		}

		return logEntries;
	}

	public ArrayList<LogEntry> populateDemoEntries(String fileName) throws IOException, URISyntaxException {
		ArrayList<LogEntry> demoEntries;
		String logJSON = JsonHandler.loadFile(fileName);
		demoEntries = JsonHandler.loadLogEntriesFromJson(logJSON);

		logger.info("demoEntries have been loaded from {}", fileName);
		return demoEntries;
	}

}
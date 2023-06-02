package exengine.engineService;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.ExplainableEngineApplication;
import exengine.algorithmicExpGenerator.FindCauseServiceAlternative;
import exengine.contextAwareExpGenerator.ExplanationContextMappingService;
import exengine.contextManager.ContextService;
import exengine.database.*;
import exengine.datamodel.*;
import exengine.datamodel.Error;
import exengine.expPresentation.*;
import exengine.haconnection.HomeAssistantConnectionService;
import exengine.loader.JsonHandler;

/**
 * Service hub responsible for building and delivering explanations.
 */
@Service
public class CreateExService {

	private static final Logger logger = LoggerFactory.getLogger(CreateExService.class);

	@Autowired
	DatabaseService dataSer;

	@Autowired
	HomeAssistantConnectionService haSer;

	@Autowired
	FindCauseServiceAlternative findCauseSer;

	@Autowired
	ContextService conSer;

	@Autowired
	ExplanationContextMappingService contextMappingSer;

	@Autowired
	TransformationFunctionService transformFuncSer;

	/**
	 * Builds context-specific explanations for home assistant.
	 * 
	 * This function determines the causal path behind an explanandum and presents
	 * an appropriate, context-dependent explanation. Refer to the paper for
	 * details.
	 * 
	 * @param min    Representing the number of minutes taken into account for
	 *               analyzing past events, starting from the call of the method
	 * @param userId The user identifier for the explainee that asked for the
	 *               explanation @Note not to confuse with the id property of the
	 *               user class
	 * @param device The device whose last action is to be explained
	 * @return Either the built explanation, or an error description in case the
	 *         explanation could not be built.
	 */
	public String getExplanation(int min, String userId, String device) {

		logger.debug("getExplanation called with arguments min: {}, user id: {}, device: {}", min, userId, device);

		ArrayList<LogEntry> logEntries = getLogEntries(min);

		User user = dataSer.findUserByUserId(userId);

		if (user == null) {
			return "unvalid userId: this user does not exist";
		}

		// STEP 0: identify explanandum's LogEntry
		LogEntry explanandum = getExplanandumsLogEntry(device, logEntries);

		if (explanandum == null) {
			return "Could not find explanandum in the logs";
		}

		// STEP 1: find causal path
		List<Rule> dbRules = dataSer.findAllRules();
		List<Error> dbErrors = dataSer.findAllErrors();
		Cause cause = findCauseSer.findCause(explanandum, logEntries, dbRules, dbErrors);

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

	/**
	 * Transform the explanandum's device name to the exact LogEntry describing the
	 * explanandum. The output is the latest entry of the Home Assistant logs that
	 * is a known action, and is associated to the provided device. In the context
	 * of this application, this is necessary for two reasons:
	 * 
	 * 1. In Home Assistant, a single physical device may be associated to various
	 * Home Assistant entities which have their own id's (e.g., a device "lab_fan"
	 * may have the entities "sensor.lab_fan_current_consumption", and
	 * "switch.lab_fan").
	 * 
	 * 2. The determination of the causal path of an explanandum relies on it's
	 * representation as an entry of the Home Assistant logs.
	 * 
	 * @param device     name of a device, or "unkown", if no particular device is
	 *                   provided
	 * @param logEntries the list of Home Assistant logs
	 * @return the latest entry of the Home Assistant logs, representing both, the
	 *         provided device as well as an action known to the system
	 */
	public LogEntry getExplanandumsLogEntry(String device, ArrayList<LogEntry> logEntries) {

		ArrayList<LogEntry> actions = dataSer.getAllActions();

		ArrayList<String> entityIds = new ArrayList<>();
		if (!device.equals("unknown")) {
			entityIds = dataSer.findEntityIdsByDeviceName(device);
		}

		Collections.sort(logEntries, Collections.reverseOrder());

		for (LogEntry logEntry : logEntries) {
			if (actions.contains(logEntry)) {
				if (entityIds.isEmpty()) {
					return logEntry;
				}
				if (entityIds.contains(logEntry.getEntityId())) {
					return logEntry;
				}
			}
		}

		return null;
	}

	/**
	 * Fetches (or when in demo, retrieves) a list of most recent log entries from
	 * Home Assistant.
	 * 
	 * @param min number of minutes, denoting the maximum age of the fetches log
	 *            entries (ignored when in demo mode)
	 * @return a list of log entries that are no older than min, starting from when
	 *         this function is called.
	 * 
	 * @Note If the ExplainableEngineApplication is running in mode mode, the
	 *       returned list of log entries will be loaded statically from a json file
	 *       stored in the resources foulder of this project.
	 */
	public ArrayList<LogEntry> getLogEntries(int min) {
		ArrayList<LogEntry> logEntries = null;

		if (ExplainableEngineApplication.isDemo()) {
			try {
				// getting demo logs (stored in a json file, stored in the resources folder)
				logEntries = loadDemoEntries(ExplainableEngineApplication.FILE_NAME_DEMO_LOGS);
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}

		} else {
			try {
				// getting logs directly from Home Assistant
				logEntries = haSer.parseLastLogs(min);
			} catch (IOException e) {
				logger.error("Unable to parse last logs: {}", e.getMessage(), e);
			}
		}

		return logEntries;
	}

	/**
	 * Loads demo entries from a json file and stores them in a list of LogEntry
	 * objects, ready for usage.
	 * 
	 * @param fileName name of json file
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public ArrayList<LogEntry> loadDemoEntries(String fileName) throws IOException, URISyntaxException {
		ArrayList<LogEntry> demoEntries;
		String logJSON = JsonHandler.loadFile(fileName);
		demoEntries = JsonHandler.loadLogEntriesFromJson(logJSON);

		logger.info("demoEntries have been loaded from {}", fileName);
		return demoEntries;
	}

}
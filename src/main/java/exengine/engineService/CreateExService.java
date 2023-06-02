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
import exengine.algorithmicExpGenerator.FindCauseService;
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
	FindCauseService findCauseSer;

	@Autowired
	ContextService conSer;

	@Autowired
	ExplanationContextMappingService contextMappingSer;

	@Autowired
	TransformationFunctionService transformFuncSer;

	/**
	 * Builds context-specific explanations for home assistant events.
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

		// STEP 0: retrieve user from database (preparation)
		User user = dataSer.findUserByUserId(userId);

		if (user == null) {
			return "unvalid userId: this user does not exist";
		}

		// STEP 1: find causal path
		List<Rule> dbRules = dataSer.findAllRules();
		List<Error> dbErrors = dataSer.findAllErrors();
		ArrayList<LogEntry> logEntries = getLogEntries(min);
		ArrayList<String> explanandumsEntityIds = getExplanandumsEntityIds(device);
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

	/**
	 * Maps a device to it's associated entityIds.
	 * 
	 * Rationale: In Home Assistant, a single physical device may be associated to
	 * various Home Assistant entities which have their own ids (e.g., a device
	 * "lab_fan" may have the entities "sensor.lab_fan_current_consumption", and
	 * "switch.lab_fan"). Further, the logs of Home Assistant, which are loaded into
	 * the getExplanation algorithm, are always referring to the entityIds, not the
	 * devices ids. However, explainees are interested in having a device explained,
	 * and be bothered by various entityIds.
	 * 
	 * @param device name of a device, or "unkown", if no particular device is
	 *               provided
	 * @return If the device was provided and exists in the database, a list of
	 *         associated entityIds of that device, else, an empty list
	 */
	public ArrayList<String> getExplanandumsEntityIds(String device) {
		if (device.equals("unknown")) {
			return new ArrayList<>();
		} else {
			return dataSer.findEntityIdsByDeviceName(device);
		}
	}

	public LogEntry getExplanandum(String device, ArrayList<LogEntry> logEntries) {
		
		ArrayList<LogEntry> actions = dataSer.getAllActions();
		
		ArrayList<String> entityIds = new ArrayList<>();
		if (!device.equals("unknown")) {
			entityIds = dataSer.findEntityIdsByDeviceName(device);
		}

		Collections.sort(logEntries, Collections.reverseOrder());

		for (LogEntry logEntry : logEntries) {

			if (actions.contains(logEntry)) {
				System.out.println("should return here " + entityIds.size());

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
	 * home assistant
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
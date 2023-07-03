package exengine.engineservice;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import exengine.ExplainableEngineApplication;
import exengine.contextexplanationgenerator.ExplanationContextMappingService;
import exengine.contextmanager.ContextService;
import exengine.database.DatabaseService;
import exengine.datamodel.LogEntry;
import exengine.explanationpresentation.TransformationFunctionService;
import exengine.haconnection.HomeAssistantConnectionService;
import exengine.loader.JsonHandler;

/**
 * Abstract service hub responsible for building and delivering explanations.
 */
public abstract class ExplanationService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExplanationService.class);


	@Autowired
	DatabaseService dataSer;

	@Autowired
	HomeAssistantConnectionService haSer;

	@Autowired
	ContextService conSer;

	@Autowired
	ExplanationContextMappingService contextMappingSer;

	@Autowired
	TransformationFunctionService transformFuncSer;

	/**
	 * Shall build context-specific explanations for home assistant.
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
	public abstract String getExplanation(int min, String userId, String device);

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
	 * @return the action that is to be explained
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
				LOGGER.error("Unable to parse demo logs: {}", e.getMessage(), e);
			}

		} else {
			try {
				// getting logs directly from Home Assistant
				logEntries = haSer.parseLastLogs(min);
			} catch (IOException e) {
				LOGGER.error("Unable to parse last logs: {}", e.getMessage(), e);
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

		LOGGER.info("demoEntries have been loaded from {}", fileName);
		return demoEntries;
	}

}

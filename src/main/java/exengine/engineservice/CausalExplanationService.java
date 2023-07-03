package exengine.engineservice;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.algorithmicexplanationgenerator.FindCauseService;
import exengine.datamodel.Context;
import exengine.datamodel.Error;
import exengine.datamodel.User;
import exengine.explanationpresentation.View;
import exengine.datamodel.LogEntry;
import exengine.datamodel.Rule;

/**
 * Service hub responsible for building and delivering <b>causal</b> explanations.
 */
@Service
public class CausalExplanationService extends ExplanationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CausalExplanationService.class);

	@Autowired
	FindCauseService findCauseSer;

	/**
	 * Builds context-specific <b>causal</b> explanations for home assistant.
	 * 
	 * This function determines the <b>causal</b> path behind an explanandum and presents
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

		LOGGER.debug("getExplanation called with arguments min: {}, user id: {}, device: {}", min, userId, device);

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
		Object cause = findCauseSer.findCause(explanandum, logEntries, dbRules, dbErrors);

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
			return "Could not determine explanation view";
		}

		// STEP 4: generate the desired explanation
		String explanation = transformFuncSer.transformExplanation(view, cause, context);

		if (explanation == null) {
			return "Could not transform explanation into natural language";
		}

		LOGGER.info("Explanation generated");
		return explanation;
	}

}
package exengine.engineservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.algorithmicexplanationgenerator.FindCauseService;
import exengine.database.DatabaseService;
import exengine.datamodel.*;
import exengine.datamodel.Error;
import exengine.explanationpresentation.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CounterfactualExplanationService extends ExplanationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CounterfactualExplanationService.class);

    @Autowired
    FindCauseService findCauseSer;

    @Autowired
    DatabaseService dataSer;

    /**
     * * Include explanation of CounterfactualExplanationService here
     * 
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

    @Override
    public String getExplanation(int min, String userId, String device) {

        LOGGER.debug("getExplanation (counterfactual) called with arguments min: {}, user id: {}, device: {}", min,    userId, device);

        ArrayList<LogEntry> logEntries = getLogEntries(min);
        System.out.println("LogEntries:" + logEntries);

        User user = dataSer.findUserByUserId(userId);

        System.out.println("User:" + user);

        if (user == null) {
            return "invalid userId: this user does not exist";
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

    //Subtractive Method:
    public String minSub(){
        return null;
    }

    //Additive Method:
    public String minAdd(){
        return null;
    }



    public String makeFire(){
        return null;
    }

    public String modify(){
        return null;
    }

    public String findRoots(){
        return null;
    }

    public String overrideOrRemove(){
        return null;
    }

    public String topsis(){
        return null;
    }



}

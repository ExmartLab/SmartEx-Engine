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
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

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

        LOGGER.debug("getExplanation (counterfactual) called with arguments min: {}, user id: {}, device: {}", min, userId, device);

        // default return value
        String explanation = "found nothing to explain";

        //LogEntry contains Strings  time, name, state, entityid and an ArrayList<String> other
        ArrayList<LogEntry> logEntries = getLogEntries(min);

        //Determine the explanandum and all necessary states
        LogEntry explanandum = getExplanandumsLogEntry(device, logEntries);
        String stateCurrent = null;
        if (explanandum != null) {
            stateCurrent = explanandum.getState();
        }
        LOGGER.info("Found explanandum: {}", explanandum);
        LOGGER.info("Found stateCurrent: {}", stateCurrent);


        LogEntry previous = getPreviousLogEntry(explanandum, logEntries);
        String statePrevious = stateCurrent;
        if (previous != null){
            statePrevious = previous.getState();
        }
        LOGGER.info("Found previous: {}", previous);
        LOGGER.info("Found statePrevious: {}", statePrevious);

        LogEntry expected = null;
        String stateExpected = null;    //action of the expectedRule from ContrastiveExplanationService

        String entityId = "none";
        ArrayList<Rule> rulesCurrent = null;
        ArrayList<Rule> rulesPrevious = null;
        ArrayList<Rule> rulesExpected = null;

        if (explanandum != null) { // an event happened

            entityId = explanandum.getEntityId();

            List<Rule> dbRules = dataSer.findAllRules();
            List<Error> dbErrors = dataSer.findAllErrors();
            rulesCurrent = findCauseSer.findCandidateRules(explanandum, dbRules);  //rules with actions leading to state_current
            rulesPrevious = findCauseSer.findCandidateRules(previous, dbRules);  //rules with actions leading to state_current
            rulesExpected = findCauseSer.findCandidateRules(expected, dbRules);  //rules with actions leading to state_current

        }






        LOGGER.info("Explanation generated");
        return "counterfactual explanation";

    }


    //Subtractive Method:
    public String minSub() {
        return null;
    }

    //Additive Method:
    public String minAdd() {
        return null;
    }


    public String makeFire() {
        return null;
    }

    public String modify() {
        return null;
    }

    public String findRoots() {
        return null;
    }

    public String overrideOrRemove() {
        return null;
    }

    public String topsis() {
        return null;
    }


    /**
     * Find the logEntry that changed the device to the state it had before the explanandum
     *
     * @param explanandum explanandum determined with getExplanandumsLogEntry
     * @param logEntries  the list of Home Assistant logs
     * @return the logEntry that changed the device to the state it had before the explanandum LogEntry
     */
    public LogEntry getPreviousLogEntry(LogEntry explanandum, ArrayList<LogEntry> logEntries) {

        ArrayList<LogEntry> actions = dataSer.getAllActions();

        String entityID = explanandum.getEntityId();
        String name = explanandum.getName();
        String state = explanandum.getState();
        String time = explanandum.getTime();

        logEntries.remove(explanandum);

        //sort the logEntries s.t. the newest ones are the first
        Collections.sort(logEntries, Collections.reverseOrder());

        //similar to getExplanandumsLogEntry, find the newest logEntry that is before the explanandum LogEntry
        //and has the same entityID as the explanandum but different state

        for (LogEntry logEntry : logEntries) {
            int timeComparison = explanandum.compareTo(logEntry);
            LOGGER.info("timeComparison {}:" , timeComparison);
            //logEntry is found if it happened before the explanandum, the entitiyID is the same and the state is differnet
            /** TODO: Check that > 0 is the correct direction */
            if (timeComparison > 0 && entityID == logEntry.getEntityId() && state != logEntry.getState()) {
                return logEntry;
            }
        }
        //no previous state found:
        return null;
    }








}

package exengine.engineservice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exengine.algorithmicexplanationgenerator.FindCauseService;
import exengine.database.DatabaseService;
import exengine.datamodel.*;
import exengine.datamodel.Error;

/**
 * This class provides contrastive explanations for rules by extending the
 * ExplanationService class. It calculates various metrics such as frequency,
 * occurrence, precondition similarity, and ownership for a list of rules and
 * uses these metrics to generate explanations.
 */
@Service
public class ContrastiveExplanationService extends ExplanationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContrastiveExplanationService.class);

	@Autowired
	DatabaseService dataSer;

	@Autowired
	FindCauseService findCauseSer;

	final int FREQUENCY_THRESHOLD = 90;
	final int OCCURRENCE_THRESHOLD = 90;

	final Double PRECONDITION_WEIGHT = 1.0;
	final Boolean PRECONDITION_BENEFICIAL = true;
	final Double OWNERSHIP_WEIGHT = 1.0;
	final Boolean OWNERSHIP_BENEFICIAL = true;
	final Double FREQUENCY_WEIGHT = 1.0;
	final Boolean FREQUENCY_BENEFICIAL = true;
	final Double OCCURRENCE_WEIGHT = 1.0;
	final Boolean OCCURRENCE_BENEFICIAL = true;

	/**
	 * Builds context-specific <b>contrastive</b> explanations for home assistant.*
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

		LOGGER.debug("getExplanation (contrastive) called with arguments min: {}, user id: {}, device: {}", min, userId,
				device);

		// default return value
		String explanation = "found nothing to explain";

		ArrayList<LogEntry> logEntries = getLogEntries(min);

		// identify explanandum's LogEntry
		LogEntry explanandum = getExplanandumsLogEntry(device, logEntries);

		String entityId = "none";
		Object happenedEvent = null;

		if (explanandum != null) { // an event happened
			LOGGER.info("Found explanandum: {}", explanandum);
			entityId = explanandum.getEntityId();

			List<Rule> dbRules = dataSer.findAllRules();
			List<Error> dbErrors = dataSer.findAllErrors();
			happenedEvent = findCauseSer.findCause(explanandum, logEntries, dbRules, dbErrors);
		}

		if (happenedEvent != null)
			if (happenedEvent instanceof Rule)
				LOGGER.debug("happened Event: {}", ((Rule) happenedEvent).getRuleName());
			else
				LOGGER.debug("happened Event: {}", ((Error) happenedEvent).getErrorName());
		else
			LOGGER.debug("happened Event: null");

		LOGGER.debug("Finding Candidates for Most Likely Rule");
		ArrayList<Rule> candidateRules = getCandidateRules(entityId);
		Rule expectedRule = getExpectedRule(explanandum, logEntries, candidateRules, happenedEvent, device, entityId, userId);

		String pattern = patternCreation(expectedRule, happenedEvent, userId, entityId, device);

		explanation = callNLP(pattern);
		LOGGER.info("Explanation generated");
		return explanation;
	}

	public ArrayList<Rule> getCandidateRules(String entityId){
		/*
		 * get rules that have actions with entityId (get all rules and remove those
		 * that don't use entityId)
		 */
		ArrayList<Rule> candidateRules = new ArrayList<Rule>(dataSer.findAllRules());
		Iterator<Rule> i = candidateRules.iterator();
		while (i.hasNext()) {
			Rule rule = i.next();
			boolean usesDeviceInAction = false;
			for (LogEntry action : rule.getActions()) {
				if (action.getEntityId().equals(entityId)) {
					usesDeviceInAction = true;
				}
			}
			if (!usesDeviceInAction) {
				i.remove(); // remove if rule doesn't have an action with entityId
			}
		}
		return candidateRules;
	}


	public Rule getExpectedRule(LogEntry explanandum, ArrayList<LogEntry> logEntries, ArrayList<Rule> candidateRules, Object happenedEvent,String device, String entityId, String userId){
		Rule expectedRule = null;
		if (candidateRules.size() > 1) {
			if (happenedEvent != null) { // explanandum & cause were found
				if (happenedEvent instanceof Rule) { // CASE RULE HAPPENED (CC1)

					LogEntry happenedAction = ((Rule) happenedEvent).getActions().get(0);
					for (LogEntry action : ((Rule) happenedEvent).getActions()) {
						if (action.getEntityId().equals(entityId)) {
							happenedAction = action;
						}
					}

					/*
					 * add all rules to reduced candidate list that dont have the same action as the
					 * happened rule
					 */
					ArrayList<Rule> reducedCandidates = new ArrayList<Rule>();
					for (Rule rule : candidateRules) {
						boolean ruleHasSameAction = false;
						for (LogEntry action : rule.getActions()) {
							if (action.equals(happenedAction)) {
								ruleHasSameAction = true;
							}
						}
						if (!ruleHasSameAction) {
							reducedCandidates.add(rule);
						}
					}
					for (Rule rule : reducedCandidates) {
						System.out.println(rule.getRuleName());
					}

					/*
					 * as of now, candidateRules contains all rules, that use the entityId
					 * in a different way in at least one action
					 */

					// calculate Precondition similarity
					ArrayList<Double> preconditionSimList = calculatePreconditionSimilarity(reducedCandidates,
							(Rule) happenedEvent);

					// calculate Ownership
					ArrayList<Double> ownershipList = calculateOwnership(reducedCandidates, userId);

					// calculate frequency
					ArrayList<Double> frequencyList = calculateFrequency(reducedCandidates, FREQUENCY_THRESHOLD);

					// calculate occurrence
					ArrayList<Double> occurrenceList = calculateOccurrence(reducedCandidates, OCCURRENCE_THRESHOLD,
							userId);

					ArrayList<Double> weights = new ArrayList<Double>();
					weights.add(PRECONDITION_WEIGHT);
					weights.add(OWNERSHIP_WEIGHT);
					weights.add(FREQUENCY_WEIGHT);
					weights.add(OCCURRENCE_WEIGHT);

					ArrayList<Boolean> isBeneficial = new ArrayList<Boolean>();
					isBeneficial.add(PRECONDITION_BENEFICIAL);
					isBeneficial.add(OWNERSHIP_BENEFICIAL);
					isBeneficial.add(FREQUENCY_BENEFICIAL);
					isBeneficial.add(OCCURRENCE_BENEFICIAL);

					expectedRule = topsis(reducedCandidates, weights, isBeneficial, preconditionSimList, ownershipList,
							frequencyList, occurrenceList);

					if (expectedRule == null) {
						LOGGER.error("Error in TOPSIS Calculation");
						return null;
					}
					LOGGER.debug("topsis determined most likely rule: {}", expectedRule.getRuleName());

				} else { // CASE ERROR HAPPENED (CC2)

					// remove old explanandum
					logEntries.remove(explanandum);

					// identify new explanandum (to find rule before error)
					LogEntry newExplanandum = getExplanandumsLogEntry(device, logEntries);

					Object newHappenedEvent = null;

					if (explanandum != null) { // an event happened
						LOGGER.info("Found new explanandum: {}", newExplanandum);
						List<Rule> dbRules = dataSer.findAllRules();
						List<Error> dbErrors = dataSer.findAllErrors();
						newHappenedEvent = findCauseSer.findCause(explanandum, logEntries, dbRules, dbErrors);
					}

					if (newHappenedEvent == null) {
						// case no rule happened before the error
						return null;
					} else if (newHappenedEvent instanceof Rule) {
						expectedRule = (Rule) newHappenedEvent;
					}

				}

			} else { // CASE NO EVENT HAPPENED (CC3)

				// calculate Ownership
				ArrayList<Double> ownershipList = calculateOwnership(candidateRules, userId);

				// calculate frequency
				ArrayList<Double> frequencyList = calculateFrequency(candidateRules, FREQUENCY_THRESHOLD);

				// calculate occurrence
				ArrayList<Double> occurrenceList = calculateOccurrence(candidateRules, OCCURRENCE_THRESHOLD,
						userId);

				ArrayList<Double> weights = new ArrayList<Double>();
				weights.add(OWNERSHIP_WEIGHT);
				weights.add(FREQUENCY_WEIGHT);
				weights.add(OCCURRENCE_WEIGHT);

				ArrayList<Boolean> isBeneficial = new ArrayList<Boolean>();
				isBeneficial.add(OWNERSHIP_BENEFICIAL);
				isBeneficial.add(FREQUENCY_BENEFICIAL);
				isBeneficial.add(OCCURRENCE_BENEFICIAL);

				expectedRule = topsis(candidateRules, weights, isBeneficial, ownershipList, frequencyList,
						occurrenceList);

				if (expectedRule == null) {
					LOGGER.error("Error in TOPSIS Calculation");
					return null;
				}
				LOGGER.debug("topsis determined most likely rule: {}", expectedRule.getRuleName());
			}
		} else if (candidateRules.size() == 1) {
			// if only one Rule refers to the entityId in question, it is the expected Rule
			expectedRule = candidateRules.get(0);
		} else {
			LOGGER.debug("Could not generate contrastive explanation");
			return null;
		}
		return expectedRule;

	}


	/**
	 * Calculates the ownership of a list of rules for a given user.
	 *
	 * @param ruleCandidates a list of rules to calculate the ownership for
	 * @param explaineeid    the ID of the user to check the ownership for
	 * @return a list of doubles where 1.0 indicates that the user is the owner of
	 *         the rule and 0.0 indicates that the user is not the owner of the rule
	 */
	private ArrayList<Double> calculateOwnership(ArrayList<Rule> ruleCandidates, String explaineeid) {
		ArrayList<Double> ownershipList = new ArrayList<Double>();
		for (Rule rule : ruleCandidates) {
			if (rule.getOwnerId().equals(explaineeid)) {
				ownershipList.add(1.0);
			} else {
				ownershipList.add(0.0);
			}
		}
		return ownershipList;
	}

	/**
	 * Calculates the frequency of how often each rule in a list of rules was called
	 * in the last x days.
	 *
	 * @param ruleCandidates a list of rules to calculate the frequency for
	 * @param thresholdDays  the number of days to look back when calculating the
	 *                       frequency
	 * @return a list of doubles representing the frequency of each rule in the
	 *         input list
	 */
	private ArrayList<Double> calculateFrequency(ArrayList<Rule> ruleCandidates, int thresholdDays) {

		// TODO discuss whether to outsource code in outer loop (e.g. to contextService)

		ArrayList<Double> frequencyList = new ArrayList<Double>();

		for (Rule candidate : ruleCandidates) {
			List<FrequencyEntry> entries = dataSer.findAllFrequencyEntriesByRuleId(candidate.getRuleId());
			int count = 0;
			LocalDateTime reference = LocalDateTime.now().minusDays(thresholdDays);
			ZoneId zone = ZoneId.systemDefault();

			for (FrequencyEntry entry : entries) {
				long timestampInMillis = entry.getTime();
				Instant instant = Instant.ofEpochMilli(timestampInMillis);
				LocalDateTime entryDateTime = LocalDateTime.ofInstant(instant, zone);
				if (entryDateTime.isAfter(reference)) {
					count++;
				}
			}
			frequencyList.add(Double.valueOf(count));
		}

		return frequencyList;
	}

	/**
	 * Calculates the occurrence of how often each rule in a list of rules was
	 * explained to a given user in the last x days.
	 *
	 * @param ruleCandidates a list of rules to calculate the occurrence for
	 * @param thresholdDays  the number of days to look back when calculating the
	 *                       occurrence
	 * @param explaineeId    the ID of the user to whom the rules were explained
	 * @return a list of doubles representing the occurrence of each rule in the
	 *         input list
	 */
	private ArrayList<Double> calculateOccurrence(ArrayList<Rule> ruleCandidates, int thresholdDays,
			String explaineeId) {

		ArrayList<Double> occurrenceList = new ArrayList<Double>();

		for (Rule candidate : ruleCandidates) {
			occurrenceList.add(
					Double.valueOf(conSer.calculateOccurrenceCount(explaineeId, candidate.getRuleId(), thresholdDays)));
		}

		return occurrenceList;
	}

	/**
	 * Calculates the similarity between the preconditions of a given rule and the
	 * preconditions of each rule in a list of rules.
	 *
	 * @param ruleCandidates a list of rules to calculate the similarity for
	 * @param happenedRule   the rule to compare the preconditions with
	 * @return a list of doubles representing the similarity between the
	 *         preconditions of each rule in the input list and the preconditions of
	 *         the given rule
	 */
	private ArrayList<Double> calculatePreconditionSimilarity(ArrayList<Rule> ruleCandidates, Rule happenedRule) {

		/*
		 * the line references refer to Algorithm 1 in the thesis
		 */

		// create List that will be filled and returned
		ArrayList<Double> preconditionSimList = new ArrayList<Double>();

		// line 5
		// get list with all precondition
		ArrayList<LogEntry> happenedRulePrec = new ArrayList<LogEntry>();
		happenedRulePrec.addAll(happenedRule.getTrigger());
		happenedRulePrec.addAll(happenedRule.getConditions());

		// create list of entityIds of preconditions of happened rule with possible
		// duplicates
		ArrayList<String> happenedRuleDevicesWithDuplicates = new ArrayList<String>();
		happenedRuleDevicesWithDuplicates = (ArrayList<String>) happenedRulePrec.stream()
				.map(prec -> prec.getEntityId()).collect(Collectors.toList());
		ArrayList<String> happenedRuleDevices = removeDuplicates(happenedRuleDevicesWithDuplicates);

		// line 6
		// loop trough candidate Rules to determine their similarity scores
		for (Rule candidate : ruleCandidates) {

			// line 7
			ArrayList<LogEntry> candidatePrec = new ArrayList<LogEntry>();
			candidatePrec.addAll(candidate.getTrigger());
			candidatePrec.addAll(candidate.getConditions());

			ArrayList<LogEntry> combP1 = new ArrayList<LogEntry>();
			combP1.addAll(candidatePrec);
			combP1.addAll(happenedRulePrec);
			ArrayList<LogEntry> combinedPrecList = removeDuplicates(combP1);

			// create list of devices of preconditions of candidate rule with possible
			// duplicates
			ArrayList<String> candidateRuleDevicesWithDuplicates = new ArrayList<String>();
			candidateRuleDevicesWithDuplicates = (ArrayList<String>) candidatePrec.stream()
					.map(prec -> prec.getEntityId()).collect(Collectors.toList());
			ArrayList<String> candidateRuleDevices = removeDuplicates(candidateRuleDevicesWithDuplicates);

			// line 8
			ArrayList<String> combD1 = new ArrayList<String>();
			combD1.addAll(candidateRuleDevices);
			combD1.addAll(happenedRuleDevices);
			ArrayList<String> combinedDevices = removeDuplicates(combD1);

			// line 9
			int distanceBetweenCandidateAndHappenedRule = 0;

			// line 10
			for (LogEntry precon : combinedPrecList) {

				// line 11
				// if the usage of a precondition is different, add 1 to the distance between
				// Candidate rule and happened rule
				if (candidatePrec.contains(precon) ^ happenedRulePrec.contains(precon)) {
					// line 12
					distanceBetweenCandidateAndHappenedRule++;
				}
			}
			for (String device : combinedDevices) {
				// if the usage of a device is different, add 1 to the distance between
				// Candidate rule and happened rule
				if (candidateRuleDevices.contains(device) ^ happenedRuleDevices.contains(device)) {
					distanceBetweenCandidateAndHappenedRule++;
				}
			}

			// line 15
			Double similarityScore = 1.0 - ((double) distanceBetweenCandidateAndHappenedRule
					/ (double) ((combinedPrecList.size() + combinedDevices.size())));

			// line 16
			// add normalized similarity score to return list
			preconditionSimList.add(similarityScore);
		}

		// line 18
		return preconditionSimList;
	}

	/**
	 * Calculates the TOPSIS (Technique for Order of Preference by Similarity to
	 * Ideal Solution) score for a list of alternatives based on their attributes.
	 *
	 * @param alternatives  a list of alternatives to calculate the TOPSIS score for
	 * @param weights       a list of weights for each attribute
	 * @param isBeneficial  a list of booleans indicating whether each attribute is
	 *                      beneficial or not
	 * @param matrixColumns a varargs parameter representing the columns of the
	 *                      decision matrix, where each column corresponds to an
	 *                      attribute and each row corresponds to an alternative
	 * @return the alternative with the highest TOPSIS score
	 */
	@SafeVarargs
	public static <T> T topsis(ArrayList<T> alternatives, ArrayList<Double> weights, ArrayList<Boolean> isBeneficial,
			ArrayList<Double>... matrixColumns) {

		/*
		 * The following steps refer to the TOPSIS steps outlined in the thesis
		 */

		// STEP 1
		// check that lengths of lists are same as dimensions of matrix
		// (size of alternatives and matrixColumns as reference)
		if (weights.size() != matrixColumns.length || isBeneficial.size() != matrixColumns.length) {
			LOGGER.error("dimensions of given parameters not correct");
			return null;
		}

		for (ArrayList<Double> column : matrixColumns) {
			if (column.size() != alternatives.size()) {
				LOGGER.error("dimensions of given parameters not correct");
				return null;
			}
		}

		// normalize weights
		ArrayList<Double> normalizedWeights = new ArrayList<Double>();
		Double weightsSum = weights.stream().mapToDouble(Double::doubleValue).sum();
		for (Double weight : weights) {
			normalizedWeights.add(weight / weightsSum);
		}

		// STEP 2
		// calculate normalized matrix from columns
		ArrayList<Double>[] normalizedMatrix = matrixColumns.clone();
		for (int i = 0; i < matrixColumns.length; i++) {
			Double squaredSums = 0.0;
			for (int j = 0; j < matrixColumns[i].size(); j++) {
				squaredSums += Math.pow(matrixColumns[i].get(j), 2.0);
			}
			Double rootOfSquaredSums = Math.sqrt(squaredSums);

			for (int j = 0; j < matrixColumns[i].size(); j++) {
				normalizedMatrix[i].set(j, matrixColumns[i].get(j) / rootOfSquaredSums);
			}
		}

		// STEP 3
		// calculate weighted normalized matrix
		ArrayList<Double>[] weightedNormalizedMatrix = normalizedMatrix.clone();
		for (int i = 0; i < normalizedMatrix.length; i++) {
			for (int j = 0; j < normalizedMatrix[i].size(); j++) {
				weightedNormalizedMatrix[i].set(j, normalizedMatrix[i].get(j) * normalizedWeights.get(i));
			}
		}

		// STEP 4
		// calculate idealBests/idealWorsts
		Double[] idealBests = new Double[normalizedWeights.size()];
		Double[] idealWorsts = new Double[normalizedWeights.size()];
		for (int i = 0; i < weightedNormalizedMatrix.length; i++) {
			if (isBeneficial.get(i)) {
				// calculate column max for ideal Best and min for idealWorst
				idealBests[i] = (Double) weightedNormalizedMatrix[i].stream().mapToDouble(Double::doubleValue).max()
						.orElseThrow(IllegalStateException::new);
				idealWorsts[i] = (Double) weightedNormalizedMatrix[i].stream().mapToDouble(Double::doubleValue).min()
						.orElseThrow(IllegalStateException::new);
			} else {
				// calculate column min for ideal Best and max for idealWorst
				idealBests[i] = (Double) weightedNormalizedMatrix[i].stream().mapToDouble(Double::doubleValue).min()
						.orElseThrow(IllegalStateException::new);
				idealWorsts[i] = (Double) weightedNormalizedMatrix[i].stream().mapToDouble(Double::doubleValue).max()
						.orElseThrow(IllegalStateException::new);
			}
		}

		// STEP 5
		// calculate Euclidean distances from ideal best
		ArrayList<Double> s_iPlus = new ArrayList<Double>();
		for (int i = 0; i < alternatives.size(); i++) {
			double totalDistancesSquared = 0.0;
			for (int j = 0; j < weightedNormalizedMatrix.length; j++) {
				double ijValue = weightedNormalizedMatrix[j].get(i);
				// System.out.println("i:" + i + "; j:" + j + "; ijValue:" + ijValue);
				totalDistancesSquared += Math.pow((ijValue - idealBests[j]), 2);
				// System.out.println("distance squared: " + totalDistancesSquared);
			}
			s_iPlus.add(Math.sqrt(totalDistancesSquared));
		}

		// calculate Euclidean distances from ideal worst
		ArrayList<Double> s_iMinus = new ArrayList<Double>();
		for (int i = 0; i < alternatives.size(); i++) {
			double totalDistancesSquared = 0.0;
			for (int j = 0; j < weightedNormalizedMatrix.length; j++) {
				totalDistancesSquared += Math.pow((weightedNormalizedMatrix[j].get(i) - idealWorsts[j]), 2);
			}
			s_iMinus.add(Math.sqrt(totalDistancesSquared));
		}

		// STEP 6
		// calculate performance score
		ArrayList<Double> p_i = new ArrayList<Double>();
		for (int i = 0; i < alternatives.size(); i++) {
			p_i.add(s_iMinus.get(i) / (s_iPlus.get(i) + s_iMinus.get(i)));
		}

		// STEP 7
		Double max = Collections.max(p_i);
		int index = p_i.indexOf(max);

		return alternatives.get(index);
	}

	private String patternCreation(Rule expectedRule, Object happenedEvent, String explaineeid, String entityId,
			String device) {

		String pattern = "";
		if (happenedEvent != null) {
			if (happenedEvent instanceof Rule) { // CC1

				String deviceName = dataSer.findEntityByEntityID(entityId).getDeviceName();
				System.out.println("findEntityByEntityId worked. Entity found: " + deviceName);
				LogEntry happenedAction = getDeviceAction((Rule) happenedEvent, entityId);
				LogEntry expectedAction = getDeviceAction(expectedRule, entityId);
				ArrayList<String> preconditions = new ArrayList<String>();
				for (LogEntry condition : ((Rule) happenedEvent).getConditions()) {
					preconditions.add(condition.getName() + " is " + condition.getState());
				}
				for (LogEntry trigger : ((Rule) happenedEvent).getTrigger()) {
					preconditions.add(trigger.getName() + " is " + trigger.getState());
				}
				String preconditionString = "";
				for (String precondition : preconditions) {
					preconditionString += ", " + precondition;
				}

				pattern = String.format(
						"The [device] is [AHR] and negate([AER]) because [PHR]. " + "[device] = %s " + "[AHR] = %s "
								+ "[AER] = %s " + "[PHR] = %s",
						deviceName, happenedAction.getState(), expectedAction.getState(), preconditionString);

			} else if (happenedEvent instanceof Error) { // CC2

				String deviceName = dataSer.findEntityByEntityID(entityId).getDeviceName();
				System.out.println("findEntityByEntityId worked. ENtity found: " + deviceName);
				LogEntry expectedAction = getDeviceAction(expectedRule, entityId);

				pattern = String.format(
						"[error] occurred so the [device] is negate([AER]). "
								+ "[error] = %s "
								+ "[device] = %s "
								+ "[AER] = %s",
						((Error) happenedEvent).getErrorName(), deviceName, expectedAction.getState());
			}
		} else { // CC3

			LogEntry expectedAction = getDeviceAction(expectedRule, entityId);
			ArrayList<String> preconditionsExpected = new ArrayList<String>();
			for (LogEntry condition : expectedRule.getConditions()) {
				preconditionsExpected.add(condition.getName() + " is " + condition.getState());
			}
			for (LogEntry trigger : expectedRule.getTrigger()) {
				preconditionsExpected.add(trigger.getName() + " is " + trigger.getState());
			}
			String preconditionString = "";
			for (String precondition : preconditionsExpected) {
				preconditionString += ", " + precondition;
			}
			// result: <Device name> is off and not <action ER> because <rule name> wasn't
			// fired.
			pattern = String.format(
					"The [device] is off and negate([AER]) because not all [PER]. "
							+ "[device] = %s "
							+ "[AER] = %s "
							+ "[PER] = %s",
					device, expectedAction.getState(), preconditionString);
		}

		System.out.println("pattern created: " + pattern);
		return pattern;
	}

	private String callNLP(String pattern) {
		// TODO prompt engineering

		// TODO call NLP API

		// TODO replace pattern return with return from NLP API
		return pattern;
	}

	/**
	 * returns the LogEntry which is the action of the given rule which uses the
	 * given entityId
	 * 
	 * @param rule
	 * @param entityId
	 * @return
	 */
	private LogEntry getDeviceAction(Rule rule, String entityId) {
		LogEntry deviceAction = null;
		for (LogEntry action : rule.getActions()) {
			if (action.getEntityId().equals(entityId)) {
				deviceAction = action;
			}
		}
		return deviceAction;
	}

	/**
	 * Removes duplicate elements from a list while preserving the order of the
	 * elements.
	 *
	 * @param <T>  the type of elements in the list
	 * @param list the list to remove duplicates from
	 * @return the list with duplicates removed
	 */
	public <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {

		Set<T> set = new LinkedHashSet<>();
		set.addAll(list);
		list.clear();
		list.addAll(set);
		return list;
	}

	/*
	 * Method to format table (for TOPSIS evaluation) to view in console
	 */
	public static String formatAsTable(List<List<String>> rows) {
		int[] maxLengths = new int[rows.get(0).size()];
		for (List<String> row : rows) {
			for (int i = 0; i < row.size(); i++) {
				maxLengths[i] = Math.max(maxLengths[i], row.get(i).length());
			}
		}

		StringBuilder formatBuilder = new StringBuilder();
		for (int maxLength : maxLengths) {
			formatBuilder.append("%-").append(maxLength + 2).append("s");
		}
		String format = formatBuilder.toString();

		StringBuilder result = new StringBuilder();
		for (List<String> row : rows) {
			result.append(String.format(format, row.toArray(new String[0]))).append(" ");
		}
		return result.toString();
	}

}

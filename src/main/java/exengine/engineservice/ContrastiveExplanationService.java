package exengine.engineservice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
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
	 * Builds context-specific <b>contrastive</b> explanations for home assistant.
	 * 
	 * TODO check params
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

		/*
		 * GENERAL
		 * 
		 * Vergleich von preconditions anhand von name / entity_id
		 * 
		 * conditions werden als LogEntry behandelt (obwohlvon homeAssistant eig keine
		 * sind)
		 * 
		 * TODO logging / good comments
		 * 
		 * TODO check entity IDs for conditions in rule yaml
		 * 
		 * 
		 * 
		 * VIEW TO DOs in Jira
		 */

		String explanation = "found nothing to explain"; // default return value

		LOGGER.debug("find happenedEvent");
		ArrayList<LogEntry> logEntries = getLogEntries(min);

		// identify explanandum's LogEntry
		LogEntry explanandum = getExplanandumsLogEntry(device, logEntries);

		Object happenedEvent = null;

		if (explanandum != null) { // an event happened
			LOGGER.info("Found explanandum: {}", explanandum);
			List<Rule> dbRules = dataSer.findAllRules();
			List<Error> dbErrors = dataSer.findAllErrors();
			happenedEvent = findCauseSer.findCause(explanandum, logEntries, dbRules, dbErrors);
		}

		// TODO remove after testing
		happenedEvent = dataSer.findRuleByName("Meeting room occupied");

		if (happenedEvent != null)
			if (happenedEvent instanceof Rule)
				LOGGER.debug("happened Event: {}", ((Rule) happenedEvent).getRuleName());
			else
				LOGGER.debug("happened Event: {}", ((Error) happenedEvent).getErrorName());
		else
			LOGGER.debug("happened Event: null");

		LOGGER.debug("Finding Candidates for Most Likely Rule");

		// get rules that have actions with device (get all rules and remove those that
		// don't use device)
		ArrayList<Rule> mostLikelyRuleCandidates = new ArrayList<Rule>(dataSer.findAllRules()); // find all rules
		Iterator<Rule> i = mostLikelyRuleCandidates.iterator();
		while (i.hasNext()) {
			Rule r = i.next();
			boolean usesDeviceInAction = false;
			for (LogEntry action : r.getActions()) {
				if (action.getEntityId().equals(device)) {
					usesDeviceInAction = true;
				}
			}
			if (!usesDeviceInAction) {
				i.remove(); // remove if rule doesn't have an action with device
			}
		}
		for (Rule r : mostLikelyRuleCandidates) {
			System.out.println(r.getRuleName());
		}

		LOGGER.debug("Using happened Event to determine expected Rule");

		// declare expectedRule variable
		Rule expectedRule = null;

		if (mostLikelyRuleCandidates.size() > 1) {
			if (happenedEvent != null) { // explanandum & cause were found
				if (happenedEvent instanceof Rule) { // a rule has happened
					// CASE RULE HAPPENED

					LogEntry happenedAction = ((Rule) happenedEvent).getActions().get(0);
					for (LogEntry action : ((Rule) happenedEvent).getActions()) {
						if (action.getEntityId().equals(device)) {
							happenedAction = action;
						}
					}

					// add all rules to reduced candidate list that dont have the same action as the
					// happened rule
					ArrayList<Rule> reducedCandidates = new ArrayList<Rule>();
					for (Rule r : mostLikelyRuleCandidates) {
						boolean ruleHasSameAction = false;
						for (LogEntry action : r.getActions()) {
							if (action.equals(happenedAction)) {
								ruleHasSameAction = true;
							}
						}
						if (!ruleHasSameAction) {
							reducedCandidates.add(r);
						}
					}
					for (Rule r : reducedCandidates) {
						System.out.println(r.getRuleName());
					}

					// as of now, mostLikelyRuleCandidates contains all rules, that use the device
					// in a different way in at least one action

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

					// TODO remove after testing
					frequencyList.clear();
					frequencyList.addAll(Arrays.asList(0.0, 4.0));

					// TODO remove after testing
					occurrenceList.clear();
					occurrenceList.addAll(Arrays.asList(0.0, 3.0));

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
						// TODO TOPSIS Fehler abfangen
					}

				} else { // an error has happened
					// CASE ERROR HAPPENED
					// TODO expectedRule = letzte Rule, die mit device in question ausgelöst wurde
				}

			} else {
				// CASE NO EVENT HAPPENED

				// calculate Ownership
				ArrayList<Double> ownershipList = calculateOwnership(mostLikelyRuleCandidates, userId);

				// calculate frequency
				ArrayList<Double> frequencyList = calculateFrequency(mostLikelyRuleCandidates, FREQUENCY_THRESHOLD);

				// calculate occurrence
				ArrayList<Double> occurrenceList = calculateOccurrence(mostLikelyRuleCandidates, OCCURRENCE_THRESHOLD,
						userId);

				ArrayList<Double> weights = new ArrayList<Double>();
				weights.add(OWNERSHIP_WEIGHT);
				weights.add(FREQUENCY_WEIGHT);
				weights.add(OCCURRENCE_WEIGHT);

				ArrayList<Boolean> isBeneficial = new ArrayList<Boolean>();
				isBeneficial.add(OWNERSHIP_BENEFICIAL);
				isBeneficial.add(FREQUENCY_BENEFICIAL);
				isBeneficial.add(OCCURRENCE_BENEFICIAL);

				expectedRule = topsis(mostLikelyRuleCandidates, weights, isBeneficial, ownershipList, frequencyList,
						occurrenceList);
				if (expectedRule == null) {
					// TODO TOPSIS Fehler abfangen
				}
			}
		} else {
			// if only one Rule refers to the device in question, it is the expected Rule
			expectedRule = mostLikelyRuleCandidates.get(0);
		}

		String pattern = patternCreation(expectedRule, happenedEvent, userId, device);

		explanation = callNLP(pattern);

		LOGGER.info("Explanation generated");
		return explanation;
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

		// TODO discuss whether to outsource code in outer loop (e.g. to context
		// Service)

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

		// create List that will be filled and returned
		ArrayList<Double> preconditionSimList = new ArrayList<Double>();

		// get list with all precondition
		ArrayList<LogEntry> happenedRulePrec = new ArrayList<LogEntry>();
		happenedRulePrec.addAll(happenedRule.getTrigger());
		happenedRulePrec.addAll(happenedRule.getConditions());

		// create list of devices of preconditions of happened rule with possible
		// duplicates
		ArrayList<String> happenedRuleDevicesWithDuplicates = new ArrayList<String>();
		happenedRuleDevicesWithDuplicates = (ArrayList<String>) happenedRulePrec.stream()
				.map(prec -> prec.getEntityId()).collect(Collectors.toList());
		ArrayList<String> happenedRuleDevices = removeDuplicates(happenedRuleDevicesWithDuplicates);

		// loop trough candidate Rules to determine their similarity scores
		for (Rule candidate : ruleCandidates) {
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

			ArrayList<String> combD1 = new ArrayList<String>();
			combD1.addAll(candidateRuleDevices);
			combD1.addAll(happenedRuleDevices);
			ArrayList<String> combinedDevices = removeDuplicates(combD1);

			int distanceBetweenCandidateAndHappenedRule = 0;
			for (LogEntry precon : combinedPrecList) {
				// if the usage of a precondition is different, add 1 to the distance between
				// Candidate rule and happened rule
				if (candidatePrec.contains(precon) ^ happenedRulePrec.contains(precon)) {
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

			// add normalized similarity score to return list
			preconditionSimList.add(1.0
					- (distanceBetweenCandidateAndHappenedRule / (combinedPrecList.size() + combinedDevices.size())));
		}

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
	private static Rule topsis(ArrayList<Rule> alternatives, ArrayList<Double> weights, ArrayList<Boolean> isBeneficial,
			ArrayList<Double>... matrixColumns) {
		/*
		 * TODO iterate über alternatives ODER columns
		 * 
		 * TODO inline comments refer to topsis steps
		 */

		@SuppressWarnings("rawtypes")
		List<List<String>> rows = new ArrayList();

		List<String> rowA;
		if (matrixColumns.length > 3) {
			rowA = Arrays.asList("Alternatives", "Precondition", "Ownership", "Frequency", "Occurrence");
		} else {
			rowA = Arrays.asList("Alternatives", "Ownership", "Frequency", "Occurrence");
		}

		rows.add(rowA);

		for (int i = 0; i < alternatives.size(); i++) {
			List<String> rowX = new ArrayList<>();
			rowX.add(alternatives.get(i).getRuleName());
			for (ArrayList<Double> column : matrixColumns) {
				rowX.add(column.get(i).toString());
			}
			rows.add(rowX);
		}
		/*
		 * List<String> weightRow = new ArrayList();; weightRow.add("weights");
		 * for(Double d : weights) { String s = d.toString(); weightRow.add(s); }
		 * for(String s : weightRow) { System.out.print(s + "\t"); }
		 * rows.add(weightRow); List<String> beneficialRow = new ArrayList();
		 * beneficialRow.add("isBeneficial"); for(Boolean b : isBeneficial) {
		 * weightRow.add(b.toString()); } //rows.add(beneficialRow);
		 * 
		 */
		System.out.println(formatAsTable(rows));

		// check that lenghts of lists are same as dimensions of matrix
		// (size of alternatives and matrixColumns as reference)
		if (weights.size() != matrixColumns.length || isBeneficial.size() != matrixColumns.length) {
			return null;
		}

		for (ArrayList<Double> column : matrixColumns) {
			if (column.size() != alternatives.size()) {
				return null;
			}
		}

		// normalize weights
		ArrayList<Double> normalizedWeights = new ArrayList<Double>();
		Double weightsSum = weights.stream().mapToDouble(Double::doubleValue).sum();
		for (Double weight : weights) {
			normalizedWeights.add(weight / weightsSum);
		}

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

		System.out.println("normalizedMatrix:");

		rows = new ArrayList();

		rows.add(rowA);

		for (int i = 0; i < alternatives.size(); i++) {
			List<String> rowX = new ArrayList<>();
			rowX.add(alternatives.get(i).getRuleName());
			for (ArrayList<Double> column : normalizedMatrix) {
				rowX.add(column.get(i).toString());
			}
			rows.add(rowX);
		}
		System.out.println(formatAsTable(rows));

		// calculate weighted normalized matrix
		ArrayList<Double>[] weightedNormalizedMatrix = normalizedMatrix.clone();
		for (int i = 0; i < normalizedMatrix.length; i++) {
			for (int j = 0; j < normalizedMatrix[i].size(); j++) {
				weightedNormalizedMatrix[i].set(j, normalizedMatrix[i].get(j) * normalizedWeights.get(i));
			}
		}

		System.out.println("weighted normalizedMatrix:");
		rows = new ArrayList();

		rows.add(rowA);

		for (int i = 0; i < alternatives.size(); i++) {
			List<String> rowX = new ArrayList<>();
			rowX.add(alternatives.get(i).getRuleName());
			for (ArrayList<Double> column : weightedNormalizedMatrix) {
				rowX.add(column.get(i).toString());
			}
			rows.add(rowX);
		}

		System.out.println(formatAsTable(rows));

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

		// calculate Euclidean distances from ideal best
		ArrayList<Double> s_iPlus = new ArrayList<Double>();
		for (int i = 0; i < alternatives.size(); i++) {
			double totalDistancesSquared = 0.0;
			for (int j = 0; j < weightedNormalizedMatrix.length; j++) {
				double ijValue = weightedNormalizedMatrix[j].get(i);
				// System.out.println("i:" + i + "; j:" + j + "; ijValue:" + ijValue);
				totalDistancesSquared += Math.pow((ijValue - idealBests[j]), 2);
				System.out.println("distance squared: " + totalDistancesSquared);
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

		// calculate performance score
		ArrayList<Double> p_i = new ArrayList<Double>();
		for (int i = 0; i < alternatives.size(); i++) {
			p_i.add(s_iMinus.get(i) / (s_iPlus.get(i) + s_iMinus.get(i)));
		}

		Double max = Collections.max(p_i);
		int index = p_i.indexOf(max);

		LOGGER.debug("topsis determined most likely rule: {}", alternatives.get(index).getRuleName());

		return alternatives.get(index);
	}

	private String patternCreation(Rule expectedRule, Object happenedEvent, String explaineeid, String device) {
		// TODO construct pattern for explanation

		String pattern = "";
		if (happenedEvent != null) {
			if (happenedEvent instanceof Rule) {
				// CC1
				// result: <Device name> is <action HR> and <negate(action ER)> because
				// <preconditions HR>
				String deviceName = dataSer.findEntityByEntityID(device).getDeviceName();
				LogEntry happenedAction = getDeviceAction((Rule) happenedEvent, device);
				LogEntry expectedAction = getDeviceAction(expectedRule, device);
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
				/*
				 * System.out.println(String.format("The %s is %s and negate(%s) because %s",
				 * deviceName, happenedAction.getState(), expectedAction.getState(),
				 * preconditionString));
				 */
				pattern = String.format(
						"The [device] is [AHR] and negate([AER]) because [PHR]\n" + "[device] = %s\n" + "[AHR] = %s\n"
								+ "[AER] = %s\n" + "[PHR] = %s\n",
						deviceName, happenedAction.getState(), expectedAction.getState(), preconditionString);
				System.out.println("pattern created:\n" + pattern);
			} else if (happenedEvent instanceof Error) {
				// CC2
				// result: <Device name> is not
			}
		} else {
			// CC3
			// result: <Device name> is off and not <action ER> because <rule name> wasn't
			// fired.
		}

		return pattern;
	}

	private String callNLP(String pattern) {
		// TODO prompt engineering

		// TODO call NLP API

		return null;
	}

	// returns the LogEntry which is the action of the given rule which uses the
	// given device
	private LogEntry getDeviceAction(Rule rule, String device) {
		LogEntry deviceAction = null;
		for (LogEntry action : rule.getActions()) {
			if (action.getEntityId().equals(device)) {
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
	private <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {

		Set<T> set = new LinkedHashSet<>();
		set.addAll(list);
		list.clear();
		list.addAll(set);
		return list;
	}

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
			result.append(String.format(format, row.toArray(new String[0]))).append("\n");
		}
		return result.toString();
	}

}

package exengine.expPresentation;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import exengine.datamodel.Context;
import exengine.datamodel.LogEntry;
import exengine.datamodel.Rule;
import exengine.datamodel.Error;

/**
 * Component that transforms the building blocks of the explanation generation
 * into a natural language explanation.
 */
@Service
public class TransformationFunctionService {

	private static final Logger logger = LoggerFactory.getLogger(TransformationFunctionService.class);

	/**
	 * Transforms a causal path into a context-specific natural language
	 * explanation.
	 * 
	 * @param view         appropriate View for the explanation presentation
	 * @param generalCause the found causal path
	 * @param context      the collected contextual information
	 * @return A natural language explanation
	 * 
	 * @Note this method is transitively tested by the CreateExService integration
	 *       test
	 */
	public String transformExplanation(View view, Object cause, Context context) {
		String explanation = null;
		if (cause instanceof Rule rule) {
			switch (view) {
			case FULLEX:
				explanation = String.format(
						"Hi %s, %s because %s set up a rule: \"%s\" and currently %s and %s, so the rule has been fired.",
						context.getExplaineeName(), getActionsString(rule), getOwnerString(context),
						rule.getRuleDescription(), getConditionsString(rule), getTriggerString(rule));
				break;
			case RULEEX:
				explanation = String.format("Hi %s, the rule: \"%s\"has been fired.", context.getExplaineeName(),
						rule.getRuleDescription());
				break;
			case FACTEX:
				explanation = String.format("Hi %s," + " %s because currently %s and %s.", context.getExplaineeName(),
						getActionsString(rule), getConditionsString(rule), getTriggerString(rule));
				break;
			case SIMPLDEX:
				explanation = String.format("Hi %s, %s set up a rule and at this moment the rule has been fired.",
						context.getExplaineeName(), getOwnerString(context));
				break;
			default:
				break;
			}
		}
		if (cause instanceof Error error) {
			switch (view) {
			case ERRFULLEX:
				explanation = String.format("Hi %s, Error \"%s\" happened. So %s. %s.", context.getExplaineeName(),
						error.getErrorName(), error.getImplication(), error.getSolution());
				break;
			case ERRSOLEX:
				explanation = String.format("Hi %s, %s.", context.getExplaineeName(), error.getSolution());
				break;
			case ERROREX:
				explanation = String.format("Hi %s, %s.", context.getExplaineeName(), error.getImplication());
				break;
			default:
				break;
			}
		}
		logger.debug("Found explanation is: {}", explanation);
		return explanation;
	}

	/**
	 * Builds a part of a natural language sentence that concatenates the conditions
	 * of a rule by "and".
	 * 
	 * @param cause
	 * @return A natural language sentence part
	 */
	public String getConditionsString(Rule rule) {
		String conditionsString = rule.getConditions().get(0);
		for (int i = 1; i < rule.getConditions().size(); i++) {
			conditionsString = String.format("%s and %s", conditionsString, rule.getConditions().get(i));
		}
		return conditionsString;
	}

	/**
	 * Builds a part of a natural language sentence that concatenates the name and
	 * state of actions of a cause (i.e., name1 is state1 and name2 is state2).
	 * 
	 * @param cause
	 * @return A natural language sentence part
	 */
	public String getActionsString(Rule rule) {
		ArrayList<LogEntry> actionList = rule.getActions();
		LogEntry firstAction = actionList.remove(0);
		String actionsString = firstAction.getName() + " is " + firstAction.getState();

		for (LogEntry action : actionList) {
			actionsString = String.format("%s and %s is %s", actionsString, action.getName(), action.getState());
		}

		return actionsString.replace("null", "active");
	}

	/**
	 * Provides sentence part depending on whether the explainee is the owner of a
	 * to-be explained rule or not.
	 * 
	 * @param context
	 * @return A natural language sentence part
	 */
	public String getOwnerString(Context context) {
		if (context.getExplaineeName().equals(context.getOwnerName()))
			return "you have";
		else
			return context.getOwnerName() + " has";

	}

	/**
	 * Provides sentence part depending on whether there is additional information
	 * on the trigger or not.
	 * 
	 * @param cause
	 * @return A natural language sentence part
	 */
	public String getTriggerString(Rule rule) {
		LogEntry trigger = rule.getTrigger().get(0); // TODO double check that getting the first index is good
		return trigger == null ? "the rule was triggered"
				: (trigger.getName() + " is " + trigger.getState()).replace("is null", "has happened");
	}

}

package exengine.expPresentation;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import exengine.datamodel.RuleCause;
import exengine.datamodel.Cause;
import exengine.datamodel.Context;
import exengine.datamodel.ErrorCause;
import exengine.datamodel.LogEntry;

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
	public String transformExplanation(View view, Cause generalCause, Context context) {
		String explanation = null;
		if (generalCause.getClass().equals(RuleCause.class)) {
			RuleCause cause = (RuleCause) generalCause;
			switch (view) {
			case FULLEX:
				explanation = String.format(
						"Hi %s, %s because %s set up a rule: \"%s\" and currently %s and %s, so the rule has been fired.",
						context.getExplaineeName(), getActionsString(cause), getOwnerString(context),
						cause.getRule().getRuleDescription(), getConditionsString(cause), getTriggerString(cause));
				break;
			case RULEEX:
				explanation = String.format("Hi %s, the rule: \"%s\"has been fired.", context.getExplaineeName(),
						cause.getRule().getRuleDescription());
				break;
			case FACTEX:
				explanation = String.format("Hi %s," + " %s because currently %s and %s.", context.getExplaineeName(),
						getActionsString(cause), getConditionsString(cause), getTriggerString(cause));
				break;
			case SIMPLDEX:
				explanation = String.format("Hi %s, %s set up a rule and at this moment the rule has been fired.",
						context.getExplaineeName(), getOwnerString(context));
				break;
			default:
				break;
			}
		}
		if (generalCause.getClass().equals(ErrorCause.class)) {
			ErrorCause cause = (ErrorCause) generalCause;
			switch (view) {
			case ERRFULLEX:
				explanation = String.format("Hi %s, Error \"%s\" happened. So %s. %s.", context.getExplaineeName(),
						cause.getError().getErrorName(), cause.getError().getImplication(),
						cause.getError().getSolution());
				break;
			case ERRSOLEX:
				explanation = String.format("Hi %s, %s.", context.getExplaineeName(), cause.getError().getSolution());
				break;
			case ERROREX:
				explanation = String.format("Hi %s, %s.", context.getExplaineeName(),
						cause.getError().getImplication());
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
	 * of a RuleCause by "and".
	 * 
	 * @param cause
	 * @return A natural language sentence part
	 */
	public String getConditionsString(RuleCause cause) {
		String conditionsString = cause.getConditions().get(0);
		for (int i = 1; i < cause.getConditions().size(); i++) {
			conditionsString = String.format("%s and %s", conditionsString, cause.getConditions().get(i));
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
	public String getActionsString(Cause cause) {
		ArrayList<LogEntry> actionList = cause.getActions();
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
	public String getTriggerString(RuleCause cause) {
		LogEntry trigger = cause.getTrigger();
		return trigger == null ? "the rule was triggered"
				: (trigger.getName() + " is " + trigger.getState()).replace("is null", "has happened");
	}

}

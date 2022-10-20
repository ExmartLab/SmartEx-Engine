package exengine.expPresentation;

import org.springframework.stereotype.Service;

import exengine.datamodel.Cause;
import exengine.datamodel.Context;

@Service
public class TransformationFunctionService {

	// compose resulting explanation string for full explanation
	public String getFullExplanation(Cause cause, Context context) {
		return cause.getRule().isError()
				? String.format("Hi %s,\nbecause %s, %s. To resolve, %s.", context.getExplaineeName(),
						getActionsString(cause), context.getRuleDescription(), cause.getRule().getErrorSolution())
				: String.format(
						"Hi %s,\n" + "%s because %s set up a rule: \"%s\"\n"
								+ "and currently %s and %s, so the rule has been fired.",
						context.getExplaineeName(), getActionsString(cause), getOwnerString(context),
						context.getRuleDescription(), getConditionsString(cause), getTriggerString(cause));
	}

	// compose resulting explanation string for Rule Explanation / error explanation
	public String getRuleExplanation(Cause cause, Context context) {
		return cause.getRule().isError()
				? String.format("Hi %s,\n%s.", context.getExplaineeName(), context.getRuleDescription())
				: String.format("Hi %s,\nthe rule: \"%s\"\nhas been fired.", context.getExplaineeName(),
						context.getRuleDescription());
	}

	// compose resulting explanation string for fact explanation / solution explanation
	public String getFactExplanation(Cause cause, Context context) {
		return cause.getRule().isError()
				? String.format("Hi %s,\n%s.", context.getExplaineeName(), cause.getRule().getErrorSolution())
				: String.format("Hi %s,\n" + "%s because currently %s and %s.", context.getExplaineeName(),
						getActionsString(cause), getConditionsString(cause), getTriggerString(cause));
	}

	// compose resulting explanation string for simplified explanation / solution explanation
	public String getSimplifiedExplanation(Cause cause, Context context) {
		return cause.getRule().isError()
				? String.format("Hi %s,\n%s.", context.getExplaineeName(), getActionsString(cause))
				: String.format("Hi %s,\n%s set up a rule and at this moment the rule has been fired.",
						context.getExplaineeName(), getOwnerString(context));
	}

	public String getConditionsString(Cause cause) {
		String conditionsString = cause.getConditions().get(0);
		for (int i = 1; i < cause.getConditions().size(); i++) {
			conditionsString += " and " + cause.getConditions().get(i);
		}
		return conditionsString;
	}

	public String getActionsString(Cause cause) {
		String actionsString = cause.getActions().get(0).getName() + " is " + cause.getActions().get(0).getState();
		for (int i = 1; i < cause.getActions().size(); i++) {
			actionsString += " and " + cause.getActions().get(i).getName() + " is "
					+ cause.getActions().get(i).getState();
		}
		return actionsString.replaceAll("null", "active"); // TODO move replacing to earlier step in the process
	}

	public String getOwnerString(Context context) {
		if (context.getExplaineeName().equals(context.getOwnerName()))
			return "you have";
		else
			return context.getOwnerName() + " has";

	}

	public String getTriggerString(Cause cause) {
		return cause.getTrigger() == null ? "the rule was triggered"
				: (cause.getTrigger().getName() + " is " + cause.getTrigger().getState()).replaceAll("is null",
						"has happened"); // TODO move replacing to earlier step in the process
	}

}

package exengine.explanationtypes;

import org.springframework.stereotype.Service;

import exengine.datamodel.Cause;
import exengine.datamodel.Context;

@Service
public class ExplanationGenerationService {

	public String getFullExplanation(Cause cause, Context context) {
		return cause.getRule().isError() ? "error full explanation"
				: String.format(
						"Hi %s,\n" + "%s because %s has set up a rule: %s\n"
								+ "and currently %s and %s, so the rule has been fired.",
						context.getExplaineeName(), getActionsString(cause), context.getOwnerName(),
						context.getRuleDescription(), getConditionsString(cause), cause.getTrigger());
	}

	public String getRuleExplanation(Cause cause, Context context) {
		return cause.getRule().isError() ? "error rule explanation"
				: String.format(
				"Hi %s,\n" + "%s because there is a rule: %s\n"
						+ "and currently %s and %s, so the rule has been fired.",
				context.getExplaineeName(), getActionsString(cause), context.getRuleDescription());
	}

	public String getFactExplanation(Cause cause, Context context) {
		return cause.getRule().isError() ? "error fact explanation"
				: String.format("Hi %s,\n" + "%s because currently %s and %s", context.getExplaineeName(),
				getActionsString(cause), getConditionsString(cause), cause.getTrigger());
	}

	public String getSimplifiedExplanation(Cause cause, Context context) {
		return cause.getRule().isError() ? "error simplified explanation"
				: String.format("Hi %s,\n%s has set up a rule and at this moment the rule has been fired.",
				context.getExplaineeName(), context.getOwnerName());
	}

	public String getConditionsString(Cause cause) {
		String conditionsString = "conditions";
		// TODO loop (concat with and)
		return conditionsString;
	}

	public String getActionsString(Cause cause) {
		String actionsString = "actions";
		// TODO loop (concat with and)
		return actionsString;
	}

}

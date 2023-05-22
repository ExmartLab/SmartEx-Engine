package exengine.contextAwareExpGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.deliveredtechnologies.rulebook.Fact;
import com.deliveredtechnologies.rulebook.FactMap;
import com.deliveredtechnologies.rulebook.NameValueReferableMap;
import com.deliveredtechnologies.rulebook.model.runner.RuleBookRunner;

import exengine.datamodel.*;
import exengine.expPresentation.View;

/**
 * Component to perform the mapping of context to most appropriate view type,
 * according to mapping policies described in the paper and defined in the
 * packages .ruleBookForRules and .ruleBookForErrors.
 */
@Service
public class ExplanationContextMappingService {

	private static final Logger logger = LoggerFactory.getLogger(ExplanationContextMappingService.class);

	/**
	 * Runs rulebook for finding a legal and most prioritized view. It demarcates
	 * between rule and errors.
	 * 
	 * @param context conditions for determining the appropriate view
	 * @param cause   depending on the dynamic type of cause, the function will use
	 *                either the ruleBookForRules or the ruleBookForErrors
	 * @return legal and most prioritized View, given the conditions (if possible,
	 *         will return null otherwise)
	 */
	public View getExplanationView(Context context, Cause cause) {

		if (cause.getClass().equals(RuleCause.class)) {

			String ruleBookRules = "exengine.contextAwareExpGenerator.ruleBookForRules";
			List<View> allowedViews = new ArrayList<>(
					Arrays.asList(View.SIMPLDEX, View.RULEEX, View.FACTEX, View.FULLEX));

			return getView(context, ruleBookRules, allowedViews);
		}

		else if (cause.getClass().equals(ErrorCause.class)) {

			String ruleBookErrors = "exengine.contextAwareExpGenerator.ruleBookForErrors";
			List<View> allowedViews = new ArrayList<>(Arrays.asList(View.ERRFULLEX, View.ERRSOLEX, View.ERROREX));

			return getView(context, ruleBookErrors, allowedViews);

		}
		logger.debug("No valid explanation type found");
		return null;
	}

	/**
	 * Runs rulebook to return legal and most proritized View, given a particular
	 * Context for a given set of allowed Views.
	 * 
	 * This method is separated from
	 * {@link #getExplanationView(Context context, Cause cause) getExplanationView}
	 * to account for varying sets of allowed views and rulebooks.
	 * 
	 * @param context      conditions for determining the appropriate view
	 * @param runner       the rulebook. NOTE: needs to be exact name of the package
	 *                     with the rulebook // classes
	 * @param allowedViews the set of allowed views
	 * @return legal and most prioritized View, given the conditions (if possible,
	 *         will return null otherwise)
	 * 
	 */
	private View getView(Context context, String runner, List<View> allowedViews) {
		RuleBookRunner ruleBook = new RuleBookRunner(runner);

		NameValueReferableMap<Context> explanationFacts = new FactMap<>();
		explanationFacts.put(new Fact<>(context));

		List<Integer> allowedViewValues = new ArrayList<>();
		allowedViews.forEach(view -> allowedViewValues.add(view.getValue()));

		// Running the rulebook
		ruleBook.setDefaultResult(allowedViewValues);
		ruleBook.run(explanationFacts);
		ruleBook.getResult().ifPresent(result -> logger.debug("The explanation type is: {} (from the allowed: {})",
				context.getTheExpType(), result));

		// Getting the resulting type from the rulebook
		int type = context.getTheExpType();

		// Returning the resulting type as the respective enum constant
		for (View view : allowedViews) {
			if (type == view.getValue()) {
				return view;
			}
		}
		return null;
	}

}

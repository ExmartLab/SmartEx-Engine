package exengine.contextexplanationgenerator;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

import com.deliveredtechnologies.rulebook.annotation.Given;
import com.deliveredtechnologies.rulebook.annotation.Result;
import com.deliveredtechnologies.rulebook.annotation.Then;
import com.deliveredtechnologies.rulebook.annotation.When;

import exengine.datamodel.Context;

public abstract class RulebookRule {

	protected abstract List<Integer> initializeAllowedTypes();

	protected final List<Integer> allowedTypes = initializeAllowedTypes();

	@Given
	protected List<Context> contexts;

	@Result
	private List<Integer> explanationType;

	@When
	protected abstract boolean when();

	@Then
	public void then() {
		List<Integer> currentAllowedTypes = explanationType;

		explanationType = currentAllowedTypes.stream().filter(allowedTypes::contains).collect(toList());

		if (explanationType.isEmpty()) {
			explanationType = currentAllowedTypes;
		}

		contexts.get(0).setExplanationType(Collections.max(explanationType));

	}

}

package exengine.contextAwareExpGenerator.ruleBookForErrors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static java.util.stream.Collectors.toList;

import com.deliveredtechnologies.rulebook.annotation.Given;
import com.deliveredtechnologies.rulebook.annotation.Result;
import com.deliveredtechnologies.rulebook.annotation.Rule;
import com.deliveredtechnologies.rulebook.annotation.Then;
import com.deliveredtechnologies.rulebook.annotation.When;

import exengine.datamodel.*;

@Rule(order = 1)
public class User_state_meeting_rule {

// According to our table, 1=Simplified Exp, 2=Fact Ex, 3= Rule Exp, 4= Full Exp
	public static final List<Integer> meetingAllowedTypes = new ArrayList<Integer>();
	{
		{

			meetingAllowedTypes.add(1);

		}
	}

	public List<Integer> currentAllowedTypes = new ArrayList<Integer>();

	@Given
	List<Context> con;

	@Result
	private List<Integer> exType;

	@When
	public boolean when() {
		return con.stream().anyMatch(context -> context.getExplaineeState() == State.MEETING);
	}

	@Then
	public void then() {

		// get the default or the "current" allowed types, that is the @Result of
		// previous rules
		currentAllowedTypes = exType;

		exType = currentAllowedTypes.stream().filter(meetingAllowedTypes::contains).collect(toList());
		con.get(0).setTheExpType(Collections.max(exType));
	}
}
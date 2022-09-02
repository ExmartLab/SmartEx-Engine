package exengine.contextAwareExpGenerator;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.deliveredtechnologies.rulebook.annotation.Given;
import com.deliveredtechnologies.rulebook.annotation.Result;
import com.deliveredtechnologies.rulebook.annotation.Rule;
import com.deliveredtechnologies.rulebook.annotation.Then;
import com.deliveredtechnologies.rulebook.annotation.When;

import exengine.datamodel.*;

@Rule(order = 3)
public class User_Technicality_Nontech_rule {

	// According to our table, 1=Simplified Exp, 2=Fact Ex, 3= Rule Exp, 4= Full Exp
	public static final List<Integer> more_times_AllowedTypes = new ArrayList<Integer>();
	{
		{
			more_times_AllowedTypes.add(1);
			more_times_AllowedTypes.add(2);
			more_times_AllowedTypes.add(3);
		}
	}

	public List<Integer> currentAllowedTypes = new ArrayList<Integer>();

	@Given
	List<Context> con;

	@Result
	private List<Integer> exType;

	@When
	public boolean when() {
		return con.stream().anyMatch(context -> context.getExplaineeTechnicality() == Technicality.NONTECH);
	}

	@Then
	public void then() {

		// get the default or the "current" allowed types, that is the @Result of
		// previous rules
		currentAllowedTypes = exType;

		exType = currentAllowedTypes.stream().filter(more_times_AllowedTypes::contains).collect(toList());

		if (exType.isEmpty()) {
			exType = currentAllowedTypes;
		}

		con.get(0).setTheExpType(Collections.max(exType));

	}
	
}

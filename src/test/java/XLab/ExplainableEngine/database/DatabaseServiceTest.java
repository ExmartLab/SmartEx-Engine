package XLab.ExplainableEngine.database;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import XLab.ExplainableEngine.algorithmicExpGenerator.TestingObjects;
import exengine.database.DatabaseService;
import exengine.database.ErrorRepository;
import exengine.database.RuleRepository;
import exengine.datamodel.LogEntry;

@ExtendWith(MockitoExtension.class)
class DatabaseServiceTest {
	
	@InjectMocks
	private DatabaseService underTest;

	@Mock
	private RuleRepository ruleRepo;
	
	@Mock
	private ErrorRepository errorRepo;
	
	private TestingObjects testingObjects;
	
	@BeforeEach
	void init() throws IOException, URISyntaxException {
		testingObjects  = new TestingObjects();
	}
	
	@DisplayName("Test Getting All Actions Combining Rule And Error Actions")
	@Test
	void testGetAllActions() {
		
		// When
		Mockito.when(ruleRepo.findAll()).thenReturn(testingObjects.getDBRules());
		Mockito.when(errorRepo.findAll()).thenReturn(testingObjects.getDBErrors());
		
		ArrayList<LogEntry> allActions = underTest.getAllActions();
		
		// Then
		Assertions.assertEquals(4, allActions.size());
		Assertions.assertEquals("Smart Plug Social Room Coffee", allActions.get(0).getName());
		Assertions.assertEquals("tv_mute", allActions.get(1).getName());
		Assertions.assertEquals("Never used strobo light", allActions.get(2).getName());
		Assertions.assertEquals("Deebot last error", allActions.get(3).getName());
	}

}

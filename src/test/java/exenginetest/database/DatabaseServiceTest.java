package exenginetest.database;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import exengine.database.EntityRepository;
import exengine.datamodel.Entity;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import exengine.database.DatabaseService;
import exengine.database.ErrorRepository;
import exengine.database.RuleRepository;
import exengine.datamodel.LogEntry;
import exenginetest.algorithmicexplanationgenerator.TestingObjects;

@DisplayName("Unit Test Database Service")
@ExtendWith(MockitoExtension.class)
class DatabaseServiceTest {
	
	@InjectMocks
	private DatabaseService underTest;

	@Mock
	private RuleRepository ruleRepo;

	@Mock
	private EntityRepository entityRepo;
	
	@Mock
	private ErrorRepository errorRepo;
	
	private TestingObjects testingObjects;
	
	@BeforeEach
	void init() throws IOException, URISyntaxException {
		testingObjects  = new TestingObjects();
	}
	
	@DisplayName("Test Getting All Actions Combining Rule And Error Actions")
	@Test
	@Disabled
	void testGetAllActions() {
		
		// When
		Mockito.when(ruleRepo.findAll()).thenReturn(testingObjects.getDBRules());
		Mockito.when(errorRepo.findAll()).thenReturn(testingObjects.getDBErrors());
		
		ArrayList<LogEntry> allActions = underTest.getAllActions();
		
		// Then
		Assertions.assertEquals(10, allActions.size());
		Assertions.assertEquals("Smart Plug Social Room Coffee", allActions.get(0).getName());
		Assertions.assertEquals("tv_mute", allActions.get(1).getName());
		Assertions.assertEquals("Never used strobo light", allActions.get(2).getName());
		Assertions.assertEquals("Deebot last error", allActions.get(3).getName());
	}


}

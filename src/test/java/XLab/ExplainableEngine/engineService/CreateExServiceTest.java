package XLab.ExplainableEngine.engineService;

import java.util.Arrays;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import exengine.database.DatabaseService;
import exengine.datamodel.LogEntry;
import exengine.engineService.CreateExService;

@DisplayName("Unit Test CreateExService Preparation Functions")
@ExtendWith(MockitoExtension.class)
class CreateExServiceTest {

	@InjectMocks
	private CreateExService underTest;

	@Mock
	private DatabaseService dataSer;

	@DisplayName("Test getExplanandumsEntityIds Where Device Exists in Database")
	@Test
	void testGetExplanandumsEntityIds() {
		// Given
		String device = "microwave";
		ArrayList<String> entityIds = new ArrayList<String>(Arrays.asList("microwave.heat", "microwave.time"));

		// When
		Mockito.when(dataSer.findEntityIdsByDeviceName("microwave")).thenReturn(entityIds);
		ArrayList<String> logEntries = underTest.getExplanandumsEntityIds(device);

		// Then
		Assertions.assertEquals(entityIds, logEntries);
	}

	@DisplayName("Test getExplanandumsEntityIds Where Device Does Not Exist in Database")
	@Test
	void testGetExplanandumsEntityIdsDeviceNotExisting() {
		// Given
		String device = "doorbell";

		// When
		ArrayList<String> logEntries = underTest.getExplanandumsEntityIds(device);

		// Then
		Assertions.assertEquals(new ArrayList<String>(), logEntries);
	}

	@DisplayName("Test getExplanandumsEntityIds Where Device Not Provided")
	@Test
	void testGetExplanandumsEntityIdsDeviceNotProvided() {
		// Given
		String device = "unkown";

		// When
		ArrayList<String> logEntries = underTest.getExplanandumsEntityIds(device);

		// Then
		Assertions.assertEquals(new ArrayList<String>(), logEntries);
	}

	@DisplayName("Test populateDemoEntries To Load Properly Formatted JSON")
	@Test
	void testPopulateDemoEntries() throws IOException, URISyntaxException {
		// Given
		String fileName = "testingData/demoLogs.json";

		// When
		ArrayList<LogEntry> logEntries = underTest.loadDemoEntries(fileName);

		// Then
		Assertions.assertEquals(10, logEntries.size());
		for (LogEntry logEntry : logEntries) {
			Assertions.assertNotNull(logEntry);
		}
	}

}

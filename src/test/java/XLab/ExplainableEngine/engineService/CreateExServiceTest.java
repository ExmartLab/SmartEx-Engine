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

@ExtendWith(MockitoExtension.class)
class CreateExServiceTest {

	@InjectMocks
	private CreateExService underTest;

	@Mock
	private DatabaseService dataSer;

	@DisplayName("Test getExplanandumsEntityIds for the case where the provided device exists with two corresponding entityIds")
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

	@DisplayName("Test getExplanandumsEntityIds for the case where the provided device does not exist")
	@Test
	void testGetExplanandumsEntityIdsDeviceNotExisting() {
		// Given
		String device = "doorbell";

		// When
		ArrayList<String> logEntries = underTest.getExplanandumsEntityIds(device);

		// Then
		Assertions.assertEquals(new ArrayList<String>(), logEntries);
	}

	@DisplayName("Test getExplanandumsEntityIds for the case where there was no device provided")
	@Test
	void testGetExplanandumsEntityIdsDeviceNotProvided() {
		// Given
		String device = "unkown";

		// When
		ArrayList<String> logEntries = underTest.getExplanandumsEntityIds(device);

		// Then
		Assertions.assertEquals(new ArrayList<String>(), logEntries);
	}

	@DisplayName("Test populateDemoEntries to load a json in expected format")
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

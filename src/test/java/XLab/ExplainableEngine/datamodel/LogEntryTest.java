package XLab.ExplainableEngine.datamodel;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import exengine.datamodel.LogEntry;

@DisplayName("Unit Test LogEntry Behavior")
class LogEntryTest {

	@DisplayName("Test Reverse Sorting LogEntries")
	@Test
	void testSortLogEntry()
			throws IOException, URISyntaxException {
		
		// Given
		LogEntry firstEntry = new LogEntry("2022-06-23T09:07:26.920189+00:00", null, null, null, null);
		LogEntry secondEntry = new LogEntry("2022-06-23T11:19:31.037231+00:00", null, null, null, null);


		List<LogEntry> logEntries = new ArrayList<>(Arrays.asList(firstEntry, secondEntry));
		Assertions.assertEquals(logEntries.get(0), firstEntry);
		
		// When
		Collections.sort(logEntries, Collections.reverseOrder());
		
		// Then
		Assertions.assertEquals(logEntries.get(0), secondEntry);
		
	}
	
	@DisplayName("Test If Two LogEntries Equal (They Should)")
	@ParameterizedTest
	@CsvSource({"off, off", "on, on", "null, null"})
	void testEqualCheckLogEntry(String stateFirst, String stateSecond) {
		
		// Given
		LogEntry firstEntry = new LogEntry("2022-06-23T09:07:26.920189+00:00", "TV", stateFirst, "media.tv", null);
		LogEntry secondEntry = new LogEntry("2020-05-21T10:05:06.920189+00:00", "Other name for TV", stateSecond, "media.tv", null);

		// When
		boolean equality = firstEntry.equals(secondEntry);
		
		// Then
		Assertions.assertTrue(equality);
	}
	
	@DisplayName("Test If Two LogEntries Equal (They Should Not)")
	@ParameterizedTest
	@CsvSource({"off, null", "off, on", "null, off"})
	void testUnEqualCheckLogEntry(String stateFirst, String stateSecond) {
		
		// Given
		LogEntry firstEntry = new LogEntry("2022-06-23T09:07:26.920189+00:00", "TV", stateFirst, "media.tv", null);
		LogEntry secondEntry = new LogEntry("2020-05-21T10:05:06.920189+00:00", "Other name for TV", stateSecond, "media.tv", null);
		
		// When
		boolean equality = firstEntry.equals(secondEntry);
		
		// Then
		Assertions.assertFalse(equality);
	}
	
	@DisplayName("Test Get Date")
	@Test
	void testGetDate() {
		// Given
		LogEntry logEntry = new LogEntry("2022-06-23T09:07:26.920189+00:00", null, null, null, null);
		
		// When
		LocalDateTime date = logEntry.getLocalDateTime();
		
		// Then
		Assertions.assertNotNull(date, "Date should not be null");
	}

}

package XLab.ExplainableEngine.datamodel;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import exengine.datamodel.LogEntry;

class LogEntryTest {

	@DisplayName("Test reverse sorting LogEntries")
	@Test
	void testSortLogEntry()
			throws IOException, URISyntaxException {
		
		// Given
		LogEntry firstEntry = new LogEntry();
		firstEntry.setTime("2022-06-23T09:07:26.920189+00:00");

		LogEntry secondEntry = new LogEntry();
		secondEntry.setTime("2022-06-23T11:19:31.037231+00:00");

		List<LogEntry> logEntries = new ArrayList<>(Arrays.asList(firstEntry, secondEntry));
		Assertions.assertEquals(logEntries.get(0), firstEntry);
		
		// When
		Collections.sort(logEntries, Collections.reverseOrder());
		
		// Then
		Assertions.assertEquals(logEntries.get(0), secondEntry);
		
	}

}

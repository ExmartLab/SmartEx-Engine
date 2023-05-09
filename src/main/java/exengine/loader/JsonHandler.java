package exengine.loader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import exengine.ExplainableEngineApplication;
import exengine.datamodel.LogEntry;

public class JsonHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(JsonHandler.class);
	
	private JsonHandler() {}
	
	public static String loadFile(String fileName) throws IOException, URISyntaxException {
		URL resourceUrl = JsonHandler.class.getClassLoader().getResource(fileName);
		Path resourcePath = Paths.get(resourceUrl.toURI());
		String filePath = resourcePath.toAbsolutePath().toString();
		logger.debug("File loaded");
        return Files.readString(Path.of(filePath));
    }
	
	public static ArrayList<LogEntry> loadLogEntriesFromJson(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(json);

        ArrayList<LogEntry> logEntries = new ArrayList<>();
        for (JsonNode node : rootNode) {
            String time = null;
            String name = null;
            String state = null;
            String entityId = null;
            ArrayList<String> other = null;

            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = node.get(fieldName);

                if (fieldName.equals("when")) {
                    time = fieldValue.asText();
                } else if (fieldName.equals("name")) {
                    name = fieldValue.asText();
                } else if (fieldName.equals("state")) {
                    state = fieldValue.asText();
                } else if (fieldName.equals("entity_id")) {
                    entityId = fieldValue.asText();
                } else {
                	if (other == null) {
                		other = new ArrayList<>();
                	}
                    other.add(fieldName + ": " + fieldValue.asText());
                }
            }

            // Create a new object with the parsed properties and add it to the list
            logEntries.add(new LogEntry(time, name, state, entityId, other));
            logger.trace("New log entry was added to logEntries list, with parameters time: {}, name. {}, state: {}, entityId: {}, other: {}", time, name, state, entityId, other);
        }

        logger.debug("logEntries loaded from JSON");
        return logEntries;
    }
	
}

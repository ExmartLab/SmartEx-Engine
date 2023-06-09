package exengine.haconnection;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import exengine.datamodel.LogEntry;
import exengine.loader.JsonHandler;

/**
 * Service to perform retrieval of Home Assistant data.
 */
@Service
public class HomeAssistantConnectionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(HomeAssistantConnectionService.class);

	// token needs to be a long lived home assistant bearer token (create one under
	// http://IP_ADDRESS:8123/profile)
	String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIzMDlmODIyMzYxYTc0MDBmYmYxNTJhOTg2ZjU1MzlmMiIsImlhdCI6MTY0NjkwOTUwMiwiZXhwIjoxOTYyMjY5NTAyfQ.Fdov6W_HistZahjSurVQp4Tiln7UivGJR3JkqhXRSDk";
	// homeassistant.local can in URLs be replaced by 192.168.0.113
	String apiurl = "http://homeassistant.local:8123/api/";
	String logsurl = "http://homeassistant.local:8123/api/logbook/"; // without timecode
	String explanationurl = apiurl + "states/sensor.virtual_explanation";
	String jsonsample = "[{\"when\": \"2022-04-07T13:40:19.738143+00:00\", \"name\": \"motion 1 ias_zone\", \"state\": \"off\", \"entity_id\": \"binary_sensor.lumi_lumi_sensor_motion_aq2_ias_zone\"}, {\"when\": \"2022-04-07T13:40:57.255034+00:00\", \"name\": \"motion 1 ias_zone\", \"state\": \"on\", \"entity_id\": \"binary_sensor.lumi_lumi_sensor_motion_aq2_ias_zone\"}, {\"when\": \"2022-04-07T13:42:07.102948+00:00\", \"name\": \"Worldclock Sensor\", \"state\": \"15:42\", \"entity_id\": \"sensor.worldclock_sensor\", \"icon\": \"mdi:clock\"}, {\"when\": \"2022-04-07T13:42:07.255856+00:00\", \"name\": \"motion 1 ias_zone\", \"state\": \"off\", \"entity_id\": \"binary_sensor.lumi_lumi_sensor_motion_aq2_ias_zone\"}, {\"when\": \"2022-04-07T13:42:17.580947+00:00\", \"name\": \"motion 1 ias_zone\", \"state\": \"on\", \"entity_id\": \"binary_sensor.lumi_lumi_sensor_motion_aq2_ias_zone\"}]";

	String response = "";

	/**
	 * Logs the connection status to the specified Home Assistant connector.
	 */
	@PostConstruct
	public void printAPIStatus() {
		String status = "";
		try {
			status = executeHttpClient(apiurl);
		} catch (Exception e) {
			status = "unavailable because could not connect to Home Assistant: " + e.getMessage();
		}
		LOGGER.info("API status {}", status);			
	}

	/**
	 * Retrieves list of logs from locally running Home Assistant and puts the
	 * entries in LogEntry objects.
	 * 
	 * @param min number of minutes representing the maximum age of retrieved log
	 *            entries
	 * @return a list of the log entries
	 * @throws IOException
	 */
	public ArrayList<LogEntry> parseLastLogs(int min) throws IOException {
		String url = getURL(min);
		String getResponse = executeHttpClient(url);
		return JsonHandler.loadLogEntriesFromJson(getResponse);
	}

	/**
	 * Sends authorized get requests.
	 * 
	 * @param url the api endpoint
	 * @return the server's response
	 */
	public String executeHttpClient(String url) {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET()
				.setHeader("Authorization", "Bearer " + token).build();
		client.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body).thenAccept(resp -> {
			response = resp;
		}).join();
		LOGGER.debug("Return of executeHttpClient for argument {} (String) is: {}", url, response);
		return response;
	}

	/**
	 * Generates a URL for retrieving the latest logs in Home Assistant.
	 * 
	 * @param min min number of minutes specifiyng the age of the retrieved logs.
	 * @return the connection URL
	 */
	public String getURL(int min) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
		ZoneId zoneId = ZoneId.systemDefault();

		LocalDateTime currentTime = LocalDateTime.now();
		LocalDateTime newTime = currentTime.minusMinutes(min);

		ZonedDateTime zonedDateTime = ZonedDateTime.of(newTime, zoneId);
		String formattedTime = zonedDateTime.format(formatter);

		// Generate the URL with the formatted timestamp
		return "http://homeassistant.local:8123/api/logbook/" + formattedTime;
	}

}

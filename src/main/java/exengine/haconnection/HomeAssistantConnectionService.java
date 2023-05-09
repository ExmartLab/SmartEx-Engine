package exengine.haconnection;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import exengine.datamodel.LogEntry;
import exengine.loader.JsonHandler;

@Service
public class HomeAssistantConnectionService {

	private static final Logger logger = LoggerFactory.getLogger(HomeAssistantConnectionService.class);

	// token needs to be a long lived home assistant bearer token (create one under
	// http://IP_ADDRESS:8123/profile)
	String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIzMDlmODIyMzYxYTc0MDBmYmYxNTJhOTg2ZjU1MzlmMiIsImlhdCI6MTY0NjkwOTUwMiwiZXhwIjoxOTYyMjY5NTAyfQ.Fdov6W_HistZahjSurVQp4Tiln7UivGJR3JkqhXRSDk";
	// homeassistant.local can in URLs be replaced by 192.168.0.113
	String apiurl = "http://homeassistant.local:8123/api/";
	String logsurl = "http://homeassistant.local:8123/api/logbook/"; // without timecode
	String explanationurl = apiurl + "states/sensor.virtual_explanation";
	String jsonsample = "[{\"when\": \"2022-04-07T13:40:19.738143+00:00\", \"name\": \"motion 1 ias_zone\", \"state\": \"off\", \"entity_id\": \"binary_sensor.lumi_lumi_sensor_motion_aq2_ias_zone\"}, {\"when\": \"2022-04-07T13:40:57.255034+00:00\", \"name\": \"motion 1 ias_zone\", \"state\": \"on\", \"entity_id\": \"binary_sensor.lumi_lumi_sensor_motion_aq2_ias_zone\"}, {\"when\": \"2022-04-07T13:42:07.102948+00:00\", \"name\": \"Worldclock Sensor\", \"state\": \"15:42\", \"entity_id\": \"sensor.worldclock_sensor\", \"icon\": \"mdi:clock\"}, {\"when\": \"2022-04-07T13:42:07.255856+00:00\", \"name\": \"motion 1 ias_zone\", \"state\": \"off\", \"entity_id\": \"binary_sensor.lumi_lumi_sensor_motion_aq2_ias_zone\"}, {\"when\": \"2022-04-07T13:42:17.580947+00:00\", \"name\": \"motion 1 ias_zone\", \"state\": \"on\", \"entity_id\": \"binary_sensor.lumi_lumi_sensor_motion_aq2_ias_zone\"}]";

	String response = "";

	public void printAPIStatus() {
		String status = "not reachable";
		try {
			status = executeHttpClient(apiurl);
		} catch (Exception e) {
			status += " because " + e.getMessage();
		}
		logger.info("API status {}", status);
	}

	public ArrayList<LogEntry> parseLastLogs(int min) throws IOException {
		return JsonHandler.loadLogEntriesFromJson(executeHttpClient(getURLlastXMin(min)));
	}

	public ArrayList<LogEntry> parseLogsLastHour() throws IOException {
		return JsonHandler.loadLogEntriesFromJson(executeHttpClient(getURLlastHour()));
	}

	public String executeHttpClient(String url) {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET()
				.setHeader("Authorization", "Bearer " + token).build();
		client.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body).thenAccept(resp -> {
			response = resp;
		}).join();
		logger.debug("Return of executeHttpClient for argument {} (String) is: {}", url, response);
		return response;
	}

	public String getURLlastHour() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String s = logsurl + formatter.format(new Date()) + "+03:00";
		String[] arr = s.split(" ");
		s = arr[0] + "T" + arr[1];
		logger.debug("Return of get URLlastHour is: {}", s);
		return s;
	}

	// returns URL for the last X minutes
	public String getURLlastXMin(int min) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int hours = min / 60;
		min = min - (hours * 60);
		String h = Integer.toString(2 + hours);
		if (hours < 10)
			h = "0" + h;
		String m = Integer.toString(min);
		if (min < 10)
			m = "0" + m;
		String s = logsurl + formatter.format(new Date()) + "+" + h + ":" + m;
		String[] arr = s.split(" ");
		s = arr[0] + "T" + arr[1];
		logger.debug("Return from getURLlastXMin for argument {} (int): {}", min, s);
		return s;
	}

	public String getURLentireDay() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return logsurl + formatter.format(new Date()) + "T00:00:00+00:00";
	}

}

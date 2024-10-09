package exenginetest.loader;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import exengine.ExplainableEngineApplication;
import exengine.loader.JsonHandler;

@DisplayName("Unit Test Json Loading")
class JsonHandlerTest {

	@DisplayName("Test Loading LogEntry Objects From Json File")
	@Test
	void testLoadLogFile() throws IOException, URISyntaxException {
		// Given
		String fileName = "testingData/" + ExplainableEngineApplication.FILE_NAME_DEMO_LOGS;

		// When
		String json = JsonHandler.loadFile(fileName);

		// Then
		String expectedJson = """
				[
				    {
				        "when": "2022-06-23T09:07:26.920189+00:00",
				        "name": "Deebot",
				        "state": "idle",
				        "entity_id": "vacuum.deebot"
				    },
				    {
				        "when": "2022-06-23T09:07:26.932243+00:00",
				        "name": "Deebot",
				        "state": "error",
				        "entity_id": "vacuum.deebot"
				    },
				    {
				        "when": "2022-06-23T09:07:26.933444+00:00",
				        "name": "Deebot last error",
				        "state": "104",
				        "entity_id": "sensor.deebot_last_error",
				        "icon": "mdi:alert-circle"
				    },
				    {
				        "when": "2022-06-23T09:50:50.014573+00:00",
				        "name": "state change",
				        "entity_id": "scene.state_change"
				    },
				    {
				        "when": "2022-06-23T09:50:50.229746+00:00",
				        "name": "Rule: Block High Coffee Consumption (sc1)",
				        "entity_id": "automation.test_scenario_watching_tv_light_off",
				        "message": "triggered by state of sensor.smart_plug_social_room_coffee_today_s_consumption",
				        "source": "state of sensor.smart_plug_social_room_coffee_today_s_consumption",
				        "context_id": "01G67ZHDBKS302M9XP2GJTZAJH",
				        "domain": "automation"
				    },
				    {
				        "when": "2022-06-23T09:50:50.848452+00:00",
				        "name": "Smart Plug Social Room Coffee",
				        "state": "off",
				        "entity_id": "switch.smart_plug_social_room_coffee",
				        "context_event_type": "automation_triggered",
				        "context_domain": "automation",
				        "context_name": "Rule: Block High Coffee Consumption (sc1)",
				        "context_message": "triggered by state of sensor.smart_plug_social_room_coffee_today_s_consumption",
				        "context_source": "state of sensor.smart_plug_social_room_coffee_today_s_consumption",
				        "context_entity_id": "automation.test_scenario_watching_tv_light_off",
				        "context_entity_id_name": "Rule: Block High Coffee Consumption (sc1)"
				    },
				    {
				        "when": "2022-06-23T11:19:30.181206+00:00",
				        "name": "Lab TV",
				        "state": "idle",
				        "entity_id": "media_player.lab_tv",
				        "context_event_type": "automation_triggered",
				        "context_domain": "automation",
				        "context_name": "Welcome",
				        "context_message": "triggered by state of binary_sensor.door",
				        "context_source": "state of binary_sensor.door",
				        "context_entity_id": "automation.presence_notification",
				        "context_entity_id_name": "Welcome"
				    },
				    {
				        "when": "2022-06-23T11:19:31.024951+00:00",
				        "name": "Lab TV",
				        "state": "playing",
				        "entity_id": "media_player.lab_tv"
				    },
				    {
				         "when": "2022-06-23T11:19:31.025189+00:00",
				         "name": "a meeting in room 1",
				         "state": "happening",
				         "entity_id": "calendar"
				    },
				    {
				        "when": "2022-06-23T11:19:31.028089+00:00",
				        "name": "Rule: Block TV Audio During Meeting (sc2)",
				        "entity_id": "automation.sc2_multi_user_conflict",
				        "message": "triggered by state of media_player.lab_tv",
				        "source": "state of media_player.lab_tv",
				        "context_id": "01G684KSEJHD3DRWH9K36578E9",
				        "domain": "automation",
				        "context_state": "playing",
				        "context_entity_id": "media_player.lab_tv",
				        "context_entity_id_name": "Lab TV"
				    },
				    {
				        "when": "2022-06-23T11:19:31.037231+00:00",
				        "name": "tv_mute",
				        "entity_id": "scene.tv_playing",
				        "icon": "mdi:television",
				        "context_event_type": "automation_triggered",
				        "context_domain": "automation",
				        "context_name": "sc2: Multi-User-Conflict",
				        "context_message": "triggered by state of media_player.lab_tv",
				        "context_source": "state of media_player.lab_tv",
				        "context_entity_id": "automation.sc2_multi_user_conflict",
				        "context_entity_id_name": "Rule: Block TV Audio During Meeting (sc2)"
				    },
				    {
				        "when": "2022-06-23T11:19:31.126754+00:00",
				        "name": "tv_bright (constructed)",
				        "entity_id": "scene.tv_brightness"
				    },
				    {
				        "when": "2022-08-02T09:07:26.933444+00:00",
				        "name": "Turn Meeting Room Status Light orange",
				        "state": "orange",
				        "entity_id": "light.meeting_room_status_light"
				    },
				    {
				        "when": "2022-08-03T09:07:26.920189+00:00",
				        "name": "Contact Sensor Meeting Room Opening",
				        "state": "off",
				        "entity_id": "binary_sensor.lumi_lumi_sensor_magnet_aq2_opening_2"
				    },
				    {
				        "when": "2022-10-01T09:07:30.920189+00:00",
				        "name": "Lamp Office turning off",
				        "state": "off",
				        "entity_id": "status.lamp"
				    },
				    {
				        "when": "2022-10-01T09:07:31.920189+00:00",
				        "name": "Lamp Office color is null",
				        "state": "null",
				        "entity_id": "color.lamp"
				    },
				    {
				        "when": "2022-10-01T09:07:32.920189+00:00",
				        "name": "Air Conditioning turning off",
				        "state": "off",
				        "entity_id": "setting.aircon"
				    },
				    {
				        "when": "2022-10-01T09:07:34.920189+00:00",
				        "name": "Lamp Office turning on",
				        "state": "on",
				        "entity_id": "status.lamp"
				    },
				    {
				        "when": "2022-10-01T09:07:36.920189+00:00",
				        "name": "Lamp Office turning yellow",
				        "state": "yellow",
				        "entity_id": "color.lamp"
				    },
				    {
				        "when": "2022-10-01T09:07:38.920189+00:00",
				        "name": "Fan turning on",
				        "state": "on",
				        "entity_id": "status.fan"
				    },
				    {
				        "when": "2022-10-01T09:07:40.920189+00:00",
				        "name": "Window closed",
				        "state": "closed",
				        "entity_id": "status.window"
				    },
				    {
				        "when": "2022-10-01T09:07:45.920189+00:00",
				        "name": "Lamp turning blue",
				        "state": "blue",
				        "entity_id": "color.lamp"
				    },
				    {
				        "when": "2022-10-01T09:07:48.920189+00:00",
				        "name": "Air Conditioning turning on low",
				        "state": "low",
				        "entity_id": "setting.aircon"
				    },
				    {
				        "when": "2022-10-01T09:07:52.920189+00:00",
				        "name": "Window closed",
				        "state": "closed",
				        "entity_id": "status.window"
				    },
				    {
				        "when": "2022-10-01T09:07:55.920189+00:00",
				        "name": "Window open",
				        "state": "open",
				        "entity_id": "status.window"
				    },
				    {
				        "when": "2022-10-01T09:07:58.920189+00:00",
				        "name": "Lamp turning red",
				        "state": "red",
				        "entity_id": "color.lamp"
				    },
				    {
				        "when": "2022-10-01T09:08:02.920189+00:00",
				        "name": "Fan turning off",
				        "state": "off",
				        "entity_id": "status.fan"
				    },
				    {
				        "when": "2022-10-01T09:08:05.920189+00:00",
				        "name": "Warm temperature",
				        "state": "warm",
				        "entity_id": "status.temperature"
				    },
				    {
				        "when": "2022-10-01T09:08:08.920189+00:00",
				        "name": "Air Conditioning turning on high",
				        "state": "high",
				        "entity_id": "setting.aircon"
				    },
				    {
				        "when": "2022-10-01T09:08:10.920189+00:00",
				        "name": "Cold temperature",
				        "state": "cold",
				        "entity_id": "status.temperature"
				    }
				]
								""";
		Assertions.assertEquals(expectedJson.replaceAll("\\s+",""), json.replaceAll("\\s+",""));
	}

}

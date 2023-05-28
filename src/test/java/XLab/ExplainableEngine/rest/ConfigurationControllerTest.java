package XLab.ExplainableEngine.rest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import exengine.ExplainableEngineApplication;
import exengine.rest.ConfigurationController;

@DisplayName("Unit Test ConfigurationController's REST functionality")
class ConfigurationControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ConfigurationController()).build();

    @DisplayName("Test Getting the Application's Status")
    @Test
    void testGetStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/status"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Explainable Engine running"));
    }

    @DisplayName("Test Turning the Demo Mode Off")
    @Test
    void testDemoOff() throws Exception {
        ExplainableEngineApplication.setDemo(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/demo/off")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string("Testing turned off"));
        assertThat(ExplainableEngineApplication.isDemo()).isFalse();
    }

    @DisplayName("Test Turning the Demo Mode On")
    @Test
    void testDemoOn() throws Exception {
        ExplainableEngineApplication.setDemo(false);
        mockMvc.perform(MockMvcRequestBuilders.post("/demo/on")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string("Testing turned on"));
        assertThat(ExplainableEngineApplication.isDemo()).isTrue();
    }
}


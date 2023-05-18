package XLab.ExplainableEngine.rest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import exengine.ExplainableEngineApplication;
import exengine.rest.ConfigurationController;

class ConfigurationControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ConfigurationController()).build();

    @Test
    void testGetStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/status"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Explainable Engine running"));
    }

    @Test
    void testTestingOff() throws Exception {
        ExplainableEngineApplication.setDemo(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/testing/off")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string("Testing turned off"));
        assertThat(ExplainableEngineApplication.isDemo()).isFalse();
    }

    @Test
    void testTestingOn() throws Exception {
        ExplainableEngineApplication.setDemo(false);
        mockMvc.perform(MockMvcRequestBuilders.post("/testing/on")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string("Testing turned on"));
        assertThat(ExplainableEngineApplication.isDemo()).isTrue();
    }
}


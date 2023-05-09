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
    public void testGetStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/status"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Explainable Engine running"));
    }

    @Test
    public void testTestingOff() throws Exception {
        ExplainableEngineApplication.setTesting(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/testingoff")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string("Testing turned off"));
        assertThat(ExplainableEngineApplication.isTesting()).isFalse();
    }

    @Test
    public void testTestingOn() throws Exception {
        ExplainableEngineApplication.setTesting(false);
        mockMvc.perform(MockMvcRequestBuilders.post("/testingon")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string("Testing turned on"));
        assertThat(ExplainableEngineApplication.isTesting()).isTrue();
    }
}


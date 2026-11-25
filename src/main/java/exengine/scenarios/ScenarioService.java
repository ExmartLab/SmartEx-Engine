package exengine.scenarios;

import exengine.ExplainableEngineApplication;
import exengine.database.DatabaseService;
import exengine.datamodel.Scenario;
import exengine.scenarios.dto.ScenarioCreationData;
import exengine.scenarios.dto.ScenarioMetaData;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ScenarioService {

    private final DatabaseService databaseService;
    private final StorageService storageService;

    public ScenarioService(DatabaseService databaseService, StorageService storageService) {
        this.databaseService = databaseService;
        this.storageService = storageService;
    }

    /**
     * Adds a scenario to SmartEx Engine. Creates a new db entry of Type Scenario with data like title and description
     * and then stores the file under the name scenarioId.json where scenarioId is an automatically generated Id.
     *
     * @param file the MultipartFile with the demoLogs to upload, must be json
     * @param metaData the ScenarioMetaData
     * @return true, if scenario was stored successfully
     */
    public boolean addScenario(MultipartFile file, ScenarioCreationData metaData) {
        if (metaData.getDescription()==null || metaData.getDescription().isBlank() || metaData.getTitle()==null || metaData.getTitle().isBlank()) return false;
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        if (!"json".equals(suffix)) return false;
        Scenario scenario = new Scenario(getNextFreeScenarioId(), metaData.getTitle(), metaData.getDescription(), file.getOriginalFilename(), metaData.getExplanationType());
        databaseService.saveScenario(scenario);
        if (storageService.store(file, scenario.getStoredFileName())) {
            return true;
        } else {
            databaseService.deleteScenario(scenario);
            return false;
        }
    }

    /**
     * Computes the next free scenarioId based on the highest ID currently stored in the database
     *
     * @return the currently highest scenarioId +1
     */
    private int getNextFreeScenarioId() {
        List<Scenario> scenarios = databaseService.findAllScenarios();
        if(scenarios.isEmpty()) return 0;
        scenarios.sort(Comparator.comparing(Scenario::getScenarioId));
        int lastId = scenarios.get(scenarios.size()-1).getScenarioId();
        return lastId++;
    }

    /**
     * Sets the logs of the specified scenario to the new file (overwrites the old file)
     *
     * @param file the new log file to replace the old one with
     * @param scenarioId the id of the scenario to update
     * @return true, if replacement successful, false if not
     */
    public boolean updateScenario(MultipartFile file, int scenarioId) {
        Optional<Scenario> scenario = databaseService.findScenarioById(scenarioId);
        if (scenario.isEmpty()) {
            return false;
        } else {
            String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            if (!"json".equals(suffix)) return false;
            if (storageService.store(file, scenario.get().getStoredFileName())) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Retrieves all Scenarios from the database and parses those into a List of ScenarioMetaData and returns that list
     *
     * @return a List of ScenarioMetaData including all Scenario-Information from saved scenarios, null if none are found
     */
    public List<ScenarioMetaData> getAllScenarios() {
        List<Scenario> scenarios = databaseService.findAllScenarios();
        if (scenarios.isEmpty()) return null;
        List<ScenarioMetaData> scenarioInfos = new ArrayList<>();
        for (Scenario scenario: scenarios) {
            scenarioInfos.add(new ScenarioMetaData(scenario.getScenarioId(), scenario.getTitle(), scenario.getDescription(), scenario.getExplanationType()));
        }
        return scenarioInfos;
    }

    /**
     * Retrieves the selected scenario record from the database and sets the FILE_NAME_DEMO_LOGS variable of the main class
     * to the selected scenario's stored name.
     *
     * @param scenarioId the id of the scenario to select
     * @return true, if scenario set successfully, false if not
     */
    public boolean selectScenario(int scenarioId) {
        Optional<Scenario> selected = databaseService.findScenarioById(scenarioId);
        if (selected.isEmpty()) {
            return false;
        }
        else {
            ExplainableEngineApplication.FILE_NAME_DEMO_LOGS = selected.get().getStoredFileName();
            return true;
        }
    }
}

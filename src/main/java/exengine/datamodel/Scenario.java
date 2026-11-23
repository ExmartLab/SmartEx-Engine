package exengine.datamodel;

import org.springframework.data.annotation.Id;

public class Scenario {
    @Id
    private int scenarioId;
    private String title, description, fileName, explanationType;

    public Scenario(String title, String description, String fileName, String explanationType) {
        this.title = title;
        this.description = description;
        this.fileName = fileName;
        this.explanationType = explanationType;
    }

    public int getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(int scenarioId) {
        this.scenarioId = scenarioId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStoredFileName() {
        return scenarioId + ".json";
    }

    public String getExplanationType() {
        return explanationType;
    }

    public void setExplanationType(String explanationType) {
        this.explanationType = explanationType;
    }
}

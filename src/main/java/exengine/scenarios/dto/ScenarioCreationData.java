package exengine.scenarios.dto;

public class ScenarioCreationData {
    private String title, description, explanationType;

    public ScenarioCreationData(String title, String description, String explanationType) {
        this.title = title;
        this.description = description;
        this.explanationType = explanationType;
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

    public String getExplanationType() {
        return explanationType;
    }

    public void setExplanationType(String explanationType) {
        this.explanationType = explanationType;
    }
}

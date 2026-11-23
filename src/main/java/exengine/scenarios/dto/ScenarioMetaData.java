package exengine.scenarios.dto;

public class ScenarioMetaData {
    private int id;
    private String title, description, explanationType;

    public ScenarioMetaData(int id, String title, String description, String explanationType) {
        this.title = title;
        this.description = description;
        this.explanationType=explanationType;
        this.id=id;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

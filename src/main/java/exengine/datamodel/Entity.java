package exengine.datamodel;

public class Entity {

	private String entityId;
	private String deviceName;

	public Entity() {
	}

	public Entity(String entityId, String deviceName) {
		setEntityId(entityId);
		setDeviceName(deviceName);
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

}

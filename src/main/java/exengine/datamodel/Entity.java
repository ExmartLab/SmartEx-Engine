package exengine.datamodel;

public class Entity {
	
	private String entityId;
	private String deviceName;
	
	public Entity(String entityId, String deviceName) {
		this.entityId = entityId;
		this.deviceName = deviceName;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getDevice() {
		return deviceName;
	}

	public void setDevice(String deviceName) {
		this.deviceName = deviceName;
	}

}

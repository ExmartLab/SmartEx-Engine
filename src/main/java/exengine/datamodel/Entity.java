package exengine.datamodel;

/**
 * The Entity class represents a Home Assistant entity in a smart home system.
 * It contains information about the entity ID and the associated device name.
 */
public class Entity {

	private String entityId;
	private String deviceName;

	/**
	 * Constructs a new Entity object with the specified parameters.
	 *
	 * @param entityId   the ID of the entity
	 * @param deviceName the name of the associated device
	 */
	public Entity(String entityId, String deviceName) {
		setEntityId(entityId);
		setDeviceName(deviceName);
	}

	/**
	 * Returns the ID of the entity.
	 *
	 * @return the ID of the entity
	 */
	public String getEntityId() {
		return entityId;
	}

	/**
	 * Sets the ID of the entity.
	 *
	 * @param entityId the ID of the entity
	 */
	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	/**
	 * Returns the name of the associated device.
	 *
	 * @return the name of the associated device
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * Sets the name of the associated device.
	 *
	 * @param deviceName the name of the associated device
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

}

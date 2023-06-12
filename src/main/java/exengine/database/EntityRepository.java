package exengine.database;

import java.util.ArrayList;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import exengine.datamodel.Entity;

/**
 * Repository interface for accessing and managing Entity (Home Assistant
 * entities) in the MongoDB database.
 *
 * This interface extends the MongoRepository interface, providing CRUD (Create,
 * Read, Update, Delete) operations and additional query methods for Entity
 * entities.
 *
 * @see org.springframework.data.mongodb.repository.MongoRepository
 */
@Repository
public interface EntityRepository extends MongoRepository<Entity, String> {

	/**
	 * Retrieves an Entity entity by its entityId.
	 *
	 * @param entityId The entityId of the Entity to retrieve.
	 * @return The Entity entity with the specified entityId, or null if not found.
	 */
	@Query(value = "{'entityId' : '?0'}")
	Entity findByEntityId(String entityId);

	/**
	 * Retrieves an Entity entity by its deviceName.
	 *
	 * @param deviceName The deviceName of the Entity to retrieve.
	 * @return The Entity entity with the specified deviceName, or null if not
	 *         found.
	 */
	@Query(value = "{'deviceName' : '?0'}")
	Entity findByDeviceName(String deviceName);

	/**
	 * Retrieves a list of all Entity entities the specified deviceName.
	 *
	 * @param deviceName The deviceName of the Entities to retrieve.
	 * @return An ArrayList of Entity entities with the specified deviceName, or an
	 *         empty ArrayList if not found.
	 */
	@Query(value = "{'deviceName' : '?0'}")
	ArrayList<Entity> findEntitiesByDeviceName(String deviceName);

}

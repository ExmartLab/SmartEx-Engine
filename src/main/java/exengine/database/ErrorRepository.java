package exengine.database;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import exengine.datamodel.Error;

/**
 * Repository interface for accessing and managing Error entities (errors known
 * about the system) in the MongoDB database.
 *
 * This interface extends the MongoRepository interface, providing CRUD (Create,
 * Read, Update, Delete) operations and additional query methods for Error
 * entities.
 *
 * @see org.springframework.data.mongodb.repository.MongoRepository
 */
@Repository
public interface ErrorRepository extends MongoRepository<Error, String> {
	
	/**
     * Retrieves an Error entity by its errorName.
     *
     * @param errorName The errorName of the Error to retrieve.
     * @return The Error entity with the specified errorName, or null if not found.
     */
	@Query(value = "{'errorName' : '?0'}")
	Error findByName(String errorName);

	/**
     * Retrieves an Error entity by its errorId.
     *
     * @param errorId The errorId of the Error to retrieve.
     * @return The Error entity with the specified errorId, or null if not found.
     */
	@Query(value = "{'errorId' : '?0'}")
	Error findByErrorId(String errorId);

}
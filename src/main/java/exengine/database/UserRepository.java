package exengine.database;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import exengine.datamodel.User;

/**
 * Repository interface for accessing and managing User entities in the MongoDB database.
 *
 * This interface extends the MongoRepository interface, providing CRUD (Create, Read, Update, Delete) operations
 * and additional query methods for User entities.
 *
 * @see org.springframework.data.mongodb.repository.MongoRepository
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

	/**
     * Retrieves a User entity by its name.
     *
     * @param userName The name of the User to retrieve.
     * @return The User entity with the specified name, or null if not found.
     */
	@Query(value = "{'name' : '?0'}")
	User findByName(String userName);

	/**
     * Retrieves a User entity by its userId.
     *
     * @param userId The userId of the User to retrieve.
     * @return The User entity with the specified userId, or null if not found.
     */
	@Query(value = "{'userid' : '?0'}")
	User findByUserId(String userId);

}
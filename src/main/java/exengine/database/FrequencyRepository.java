package exengine.database;

import java.util.ArrayList;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import exengine.datamodel.FrequencyEntry;

/**
 * Repository interface for accessing and managing FrequencyRepository entities
 * in the MongoDB database.
 *
 * This interface extends the MongoRepository interface, providing CRUD (Create,
 * Read, Update, Delete) operations and additional query methods for
 * FrequencyRepository entities.
 *
 * @see org.springframework.data.mongodb.repository.MongoRepository
 */
@Repository
public interface FrequencyRepository extends MongoRepository<FrequencyEntry, String> {

	/**
	 * Retrieves a list of all FrequencyRepository entities stored in the database
	 * by ruleId.
	 *
	 * @param ruleId The ruleId of the FrequencyRepository entities to retrieve.
	 * @return An ArrayList of FrequencyRepository entities with the specified
	 *         ruleId, or an empty ArrayList if not found.
	 */
	@Query(value = "{'ruleId' : '?0'}")
	public ArrayList<FrequencyEntry> findFrequencyEntriesById(String ruleId);

}

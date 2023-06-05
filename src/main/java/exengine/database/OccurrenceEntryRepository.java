package exengine.database;

import java.util.ArrayList;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import exengine.datamodel.OccurrenceEntry;

/**
 * Repository interface for accessing and managing OccurrenceEntry entities in the MongoDB database.
 *
 * This interface extends the MongoRepository interface, providing CRUD (Create, Read, Update, Delete) operations
 * and additional query methods for OccurrenceEntry entities.
 *
 * @see org.springframework.data.mongodb.repository.MongoRepository
 */
@Repository
public interface OccurrenceEntryRepository extends MongoRepository<OccurrenceEntry, String> {

	 /**
     * Retrieves a list of OccurrenceEntry entities by userId and ruleId.
     *
     * @param userId  The userId of the OccurrenceEntry entities to retrieve.
     * @param ruleId  The ruleId of the OccurrenceEntry entities to retrieve.
     * @return An ArrayList of OccurrenceEntry entities with the specified userId and ruleId, or an empty ArrayList if not found.
     */
	@Query(value = "{'userId' : '?0'} && {'ruleId' : '?0'}")
	ArrayList<OccurrenceEntry> findOccurrenceEntriesByUserIdAndRuleId(String userId, String ruleId);

}
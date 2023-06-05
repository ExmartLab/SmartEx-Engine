package exengine.database;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import exengine.datamodel.Rule;

/**
 * Repository interface for accessing and managing Rule (i.e., Home Assistant
 * automations) entities in the MongoDB database.
 *
 * This interface extends the MongoRepository interface, providing CRUD (Create,
 * Read, Update, Delete) operations and additional query methods for Rule
 * entities.
 *
 * @see org.springframework.data.mongodb.repository.MongoRepository
 */
@Repository
public interface RuleRepository extends MongoRepository<Rule, String> {

	/**
	 * Retrieves a Rule entity by its name.
	 *
	 * @param ruleName The name of the Rule to retrieve.
	 * @return The Rule entity with the specified name, or null if not found.
	 */
	@Query(value = "{'ruleName' : '?0'}")
	Rule findByName(String ruleName);

	/**
	 * Retrieves a Rule entity by its ruleId.
	 *
	 * @param ruleId The ruleId of the Rule to retrieve.
	 * @return The Rule entity with the specified ruleId, or null if not found.
	 */
	@Query(value = "{'ruleId' : '?0'}")
	Rule findByRuleId(String ruleId);

}
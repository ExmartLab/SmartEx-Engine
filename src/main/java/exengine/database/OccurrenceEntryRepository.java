package exengine.database;

import java.util.ArrayList;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import exengine.datamodel.OccurrenceEntry;

@Repository
public interface OccurrenceEntryRepository extends MongoRepository<OccurrenceEntry, String> {

	@Query(value = "{'userId' : '?0'} && {'ruleId' : '?0'}")
	ArrayList<OccurrenceEntry> findOccurrenceEntriesByUserIdAndRuleId(int userId, int ruleId);

}
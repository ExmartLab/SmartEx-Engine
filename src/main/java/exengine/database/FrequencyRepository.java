package exengine.database;

import java.util.ArrayList;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import exengine.datamodel.FrequencyEntry;

@Repository
public interface FrequencyRepository extends MongoRepository<FrequencyEntry, String> {
	
	@Query(value = "{'ruleId' : '?0'}")
	public ArrayList<FrequencyEntry> findAllFrequencyEntriesById(String ruleId);
	
}

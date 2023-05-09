package exengine.database;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import exengine.datamodel.Rule;

@Repository
public interface RuleRepository extends MongoRepository<Rule, String> {

	@Query(value = "{'ruleName' : '?0'}")
	Rule findByName(String ruleName);

	@Query(value = "{'ruleId' : '?0'}")
	Rule findByRuleId(String ruleId);

}
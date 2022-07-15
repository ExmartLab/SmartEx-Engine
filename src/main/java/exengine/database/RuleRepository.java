package exengine.database;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import exengine.datamodel.Rule;

public interface RuleRepository extends MongoRepository<Rule, String> {

  public List<Rule> findByRuleName(String ruleName);
  public List<Rule> findByTrigger(String trigger);

}
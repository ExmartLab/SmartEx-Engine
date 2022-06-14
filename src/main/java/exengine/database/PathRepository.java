package exengine.database;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PathRepository extends MongoRepository<Path, String> {

  public List<Path> findByRuleName(String ruleName);
  public List<Path> findByTrigger(String trigger);

}
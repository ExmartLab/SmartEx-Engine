package exengine.database;

import exengine.datamodel.Scenario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScenarioRepository extends MongoRepository<Scenario, Integer> {

}

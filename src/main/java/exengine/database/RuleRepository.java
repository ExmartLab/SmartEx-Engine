package exengine.database;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import exengine.datamodel.Rule;

@Repository
public interface RuleRepository extends MongoRepository<Rule, String> {

	
//	@Query(value=“{‘viewName’ : ‘?0’}“)
//	ViewEnt findByViewName(String viewName);
//	
//	@Query(value=“{‘userId’ : ‘?0’} && {‘viewName’ : ‘?0’}“)
//	ViewEnt findByViewNameandUsrId(String userId, String viewName);
//	
//	@Query(value=“{‘userId’ : ‘?0’}“)
//	Collection<ViewEnt> findViewByUserID(String userId);
//	
//	@Query(value=“{‘userId’ : ‘?0’} && {‘deviceType’ : ‘?0’}“)
//	Collection<ViewEnt> findViewIdByUserIdperType(String userId, String deviceType);
	
	
	@Query(value="{'ruleName' : '?0'}")
	Rule findByName(String ruleName);

}
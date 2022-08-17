package exengine.database;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import exengine.datamodel.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

	@Query(value = "{'ruleName' : '?0'}")
	User findByName(String userName);
	
	@Query(value = "{'userId' : '?0'}")
	User findByUserId(int userId);
	
}

package exengine.database;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import exengine.datamodel.Error;

@Repository
public interface ErrorRepository extends MongoRepository<Error, String> {
	@Query(value="{'errorName' : '?0'}")
	Error findByName(String errorName);	
	
	@Query(value="{'errorId' : '?0'}")
	Error findByErrorId(String errorId);

}
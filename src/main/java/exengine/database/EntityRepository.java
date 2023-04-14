package exengine.database;

import java.util.ArrayList;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import exengine.datamodel.Entity;

@Repository
public interface EntityRepository extends MongoRepository<Entity, String> {
	@Query(value="{'entityId' : '?0'}")
	Entity findByEntityId(String entityId);	
	
	@Query(value="{'deviceName' : '?0'}")
	Entity findByDeviceName(String deviceName);
	
	@Query(value="{'deviceName' : '?0'}")
	ArrayList<Entity> findEntitiesByDeviceName(String deviceName);

}

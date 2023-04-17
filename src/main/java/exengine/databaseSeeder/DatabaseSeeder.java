package exengine.databaseSeeder;

import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;

import exengine.database.DatabaseService;
import exengine.datamodel.Role;
import exengine.datamodel.Technicality;
import exengine.datamodel.User;

@Component
public class DatabaseSeeder {
	
	private final DatabaseService dataSer;
	private final ResourceLoader resourceLoader;
	
	@Autowired
	public DatabaseSeeder(DatabaseService dataSer, ResourceLoader resourceLoader) {
		this.dataSer = dataSer;
		this.resourceLoader = resourceLoader;
	}
	
	@PostConstruct
	public void seedUsers() throws Exception {
        List<Map<String, Object>> dataList = loadDataMap("seeds/users.yaml");
		
		for (Map<String, Object> dataMap: dataList) {
			User user = new User();
			if (dataMap.containsKey("name")) {
				user.setName(dataMap.get("name").toString());
			}
			if (dataMap.containsKey("userid")) {
				user.setUserId(dataMap.get("userid").toString());
			}
			if (dataMap.containsKey("role")) {
				String roleString = dataMap.get("role").toString();
				user.setRole(Role.valueOf(roleString));
			}
			if (dataMap.containsKey("technicality")) {
				String technicalityString = dataMap.get("technicality").toString();
				user.setTechnicality(Technicality.valueOf(technicalityString));
			}
			dataSer.saveNewUser(user);
		}
		System.out.println("Users seeded");
	}
	
	public List<Map<String, Object>> loadDataMap(String path) throws Exception {
		Resource resource = resourceLoader.getResource("classpath:" + path);
		InputStream inputStream = resource.getInputStream();	
		Yaml yaml = new Yaml();
        return yaml.load(inputStream);
	}

}

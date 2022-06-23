package exengine;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import exengine.database.Rule;
import exengine.database.RuleRepository;
import exengine.database.Trigger;
import exengine.haconnection.HA_API;


@SpringBootApplication
public class ExplainableEngineApplication implements CommandLineRunner {

	
	@Autowired
	private RuleRepository ruleRepo;

	public static void main(String[] args) {
		SpringApplication.run(ExplainableEngineApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		initializeTestRepository();
		HA_API.printAPIStatus();
	}

	public void initializeTestRepository() {
		//repository.deleteAll();

		//repository.save(new Path("testRule1", "playing", "tvMuted"));
		//repository.save(new Path("testRule1", "meeting", "tvMuted"));
		List<String> conditions = new ArrayList<String>();
		conditions.add("meeting");
		conditions.add("tv playing");
		ruleRepo.save(new Rule("R1", new Trigger(conditions), "tv mute"));
	}

	/*
    repository.deleteAll();

    // save a couple of customers
    repository.save(new Customer("Alice", "Smith"));
    repository.save(new Customer("Bob", "Smith"));

    // fetch all customers
    System.out.println("Customers found with findAll():");
    System.out.println("-------------------------------");
    for (Customer customer : repository.findAll()) {
      System.out.println(customer);
    }
    System.out.println();

    // fetch an individual customer
    System.out.println("Customer found with findByFirstName('Alice'):");
    System.out.println("--------------------------------");
    System.out.println(repository.findByFirstName("Alice"));
    System.out.println();

    System.out.println("Customers found with findByLastName('Smith'):");
    System.out.println("--------------------------------");
    for (Customer customer : repository.findByLastName("Smith")) {
      System.out.println(customer);
	
	*/
	
	
}
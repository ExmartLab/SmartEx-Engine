package exengine;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import exengine.database.Rule;
import exengine.database.RuleRepository;
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
		ruleRepo.deleteAll();
		ruleRepo.save(new Rule("sc1: Goal-Order-Conflict null", null, "Smart Plug Social Room Coffee off"));
		List<String> triggers = new ArrayList<String>();
		triggers.add("Lab TV playing");
		ruleRepo.save(new Rule("sc2: Multi-User-Conflict null", triggers, "tv_mute "));
		
		triggers = new ArrayList<String>();
		triggers.add("Deebot idle");
		ruleRepo.save(new Rule("Deebot error", triggers, "Deebot last error 104"));
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
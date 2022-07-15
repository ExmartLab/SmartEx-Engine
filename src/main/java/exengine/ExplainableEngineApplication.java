package exengine;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import exengine.database.RuleRepository;
import exengine.datamodel.Rule;
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

		List<String> triggers = new ArrayList<String>();
		List<String> conditions = new ArrayList<String>();
		List<String> actions = new ArrayList<String>();

		ruleRepo.deleteAll();

		conditions.add("daily energy consumption bigger than threshold");
		actions.add("Smart Plug Social Room Coffee off");
		ruleRepo.save(new Rule("sc1: Goal-Order-Conflict null", null, conditions, actions));

		triggers.add("Lab TV playing");
		actions = new ArrayList<String>();
		conditions = new ArrayList<String>();
		conditions.add("meeting going on");
		actions.add("tv_mute null");
		ruleRepo.save(new Rule("sc2: Multi-User-Conflict null", triggers, conditions, actions));

		triggers = new ArrayList<String>();
		triggers.add("Deebot idle");

		conditions = new ArrayList<String>();
		/*
		 * Do we need a condition here? There is no rule
		 */
//		conditions.add("Deebot running");
		actions = new ArrayList<String>();
		actions.add("Deebot last error 104");
		ruleRepo.save(new Rule("Deebot error", triggers, conditions, actions));
	}

	/*
	 * repository.deleteAll();
	 * 
	 * // save a couple of customers repository.save(new Customer("Alice",
	 * "Smith")); repository.save(new Customer("Bob", "Smith"));
	 * 
	 * // fetch all customers System.out.println("Customers found with findAll():");
	 * System.out.println("-------------------------------"); for (Customer customer
	 * : repository.findAll()) { System.out.println(customer); }
	 * System.out.println();
	 * 
	 * // fetch an individual customer
	 * System.out.println("Customer found with findByFirstName('Alice'):");
	 * System.out.println("--------------------------------");
	 * System.out.println(repository.findByFirstName("Alice"));
	 * System.out.println();
	 * 
	 * System.out.println("Customers found with findByLastName('Smith'):");
	 * System.out.println("--------------------------------"); for (Customer
	 * customer : repository.findByLastName("Smith")) {
	 * System.out.println(customer);
	 * 
	 */

}
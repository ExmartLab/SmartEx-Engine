package exengine;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import exengine.database.DatabaseService;
import exengine.datamodel.Rule;
import exengine.haconnection.HomeAssistantConnectionService;

@SpringBootApplication
public class ExplainableEngineApplication implements CommandLineRunner {

	@Autowired
	private HomeAssistantConnectionService haService;
	
	@Autowired
	DatabaseService dataSer;
	
	public static void main(String[] args) {
		SpringApplication.run(ExplainableEngineApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		//initializeTestRuleRepository();
		haService.printAPIStatus();
		
//		System.out.println(ruleRepo.findRuleById("ObjectId('62f66091fc241a67fa73fc4c')").get(0).ruleName);
//		System.out.println(ruleRepo.findById("62fa0875a078fc18d5a07165").get().ruleName);
//
//		System.out.println(ruleRepo.findRuleByName("sc1: Goal-Order-Conflict null").get(0).ruleName);
//		System.out.println(ruleRepo.findRuleByName("sc1: Goal-Order-Conflict null").get(0).id);
		
	}

	public void initializeTestRuleRepository() {

		ArrayList<String> triggers = new ArrayList<String>();
		ArrayList<String> conditions = new ArrayList<String>();
		ArrayList<String> actions = new ArrayList<String>();

		dataSer.deleteAllRules();

		/*
		 * TODO create users add owners to rules
		 */

		int owner1Id = 1; // (Mersedeh)
		int owner2Id = 1; // (Mersedeh)
		int owner3Id = 2; // (Lars)

		conditions.add("daily energy consumption bigger than threshold");
		actions.add("Smart Plug Social Room Coffee off");
		dataSer.saveNewRule(new Rule(1, "sc1: Goal-Order-Conflict null", null, conditions, actions, owner1Id));

		triggers.add("Lab TV playing");
		actions = new ArrayList<String>();
		conditions = new ArrayList<String>();
		conditions.add("meeting going on");
		actions.add("tv_mute null");
		dataSer.saveNewRule(new Rule(2, "sc2: Multi-User-Conflict null", triggers, conditions, actions, owner2Id));

		triggers = new ArrayList<String>();
		triggers.add("Deebot idle");

		conditions = new ArrayList<String>();
		/*
		 * Do we need a condition here? There is no rule
		 */
//		conditions.add("Deebot running");
		actions = new ArrayList<String>();
		actions.add("Deebot last error 104");
		dataSer.saveNewRule(new Rule(5, "Deebot error", triggers, conditions, actions, owner3Id));
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
package exengine.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import exengine.database.Path;
import exengine.haconnection.HA_API;
import exengine.haconnection.LogEntry;
import exengine.service.CreateExService;

//@RequestMapping("/ExEngine")
@RestController
public class RESTController {
	

	boolean debug = true;
	
	@Autowired
	CreateExService createExSer;
	
	@GetMapping("/status")
	public ResponseEntity<String> getStatus() {
		if(debug)
			System.out.println("HTTP GET: Status returned");
		return new ResponseEntity<>("Explainable Engine running", HttpStatus.OK);
	}
	
	@GetMapping("/explain")
	public ResponseEntity<String> getExplanation() {
		if(debug)
			System.out.println("HTTP GET: Explanation requested");
		String explanation = createExSer.getExplanation(HA_API.parseLastLogs(90));
		return new ResponseEntity<>(explanation, HttpStatus.OK);
	}
	
	
	@PostMapping("/debugoff")
	public ResponseEntity<String> debugOff() {
		if(debug)
			System.out.println("HTTP POST: Debugging turned off");
		debug = false;
		return new ResponseEntity<>("Debugging turned off", HttpStatus.CREATED);
	}
	
	@PostMapping("/debugon")
	public ResponseEntity<String> debugOn() {
		System.out.println("HTTP POST: Debugging turned on");
		debug = true;
		return new ResponseEntity<>("Debugging turned on", HttpStatus.CREATED);
	}
	
	
	
	
	
	
	@GetMapping("/greeting")
	public ResponseEntity<String> greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		if(debug)
			System.out.println("HTTP GET: Greeting for " + name + " requested");
		return new ResponseEntity<>("Hello " + name + "!", HttpStatus.OK);
	}
	
	
	/*
	private static final String template = "Hellooo, %s!";

	@GetMapping("/greeting")
	public Status greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		return new Status(String.format(template, name));
	}
	*/

}

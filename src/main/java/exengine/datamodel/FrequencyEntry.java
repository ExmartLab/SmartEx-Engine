package exengine.datamodel;

public class FrequencyEntry {
	
	String ruleId;
	long timeMiliSec;
	
	public FrequencyEntry(String ruleId, long timeMiliSec) {
		this.ruleId = ruleId;
		this.timeMiliSec = timeMiliSec;
	}

}

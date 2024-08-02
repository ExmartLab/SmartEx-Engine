package exengine.datamodel;

/**
 * The FrequencyEntry class represents a rule that has been fired in Home
 * Assistant. It is used to calculate the number of times a rule was fired by
 * Home Assistant (for potentially a given time interval). This information is
 * relevant for the contrastive explanation generation process.
 * 
 * This class contains the rule ID, and the time of the occurrence.
 */
public class FrequencyEntry {

	String ruleId;

	/*
	 * Time in in miliseconds since the Unix epoch, which is defined as January 1,
	 * 1970, 00:00:00 UTC
	 */
	long timeMiliSec;

	/**
	 * Constructs an FrequencyEntry with the specified parameters.
	 *
	 * @param ruleId the rule ID that identifies the fired rule in Home Assistant
	 * @param time   the time of the occurrence (which may also be the time at which
	 *               the ExplainableEngineApplication registers that the provided
	 *               rule was fired).
	 */
	public FrequencyEntry(String ruleId, long timeMiliSec) {
		this.ruleId = ruleId;
		this.timeMiliSec = timeMiliSec;
	}

	
	/**
	 * Returns the time of the frequency information. Time in in miliseconds since
	 * the Unix epoch, which is defined as January 1, 1970, 00:00:00 UTC
	 *
	 * @return the time of the occurrence information
	 */
	public long getTime() {
		return timeMiliSec;
	}
}

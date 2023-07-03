package exengine.engineservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service hub responsible for building and delivering <b>contrastive</b> explanations.
 */
@Service
public class ContrastiveExplanationService extends ExplanationService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ContrastiveExplanationService.class);

	/**
	 * Builds context-specific <b>contrastive</b> explanations for home assistant.
	 * 
	 * @param min    Representing the number of minutes taken into account for
	 *               analyzing past events, starting from the call of the method
	 * @param userId The user identifier for the explainee that asked for the
	 *               explanation @Note not to confuse with the id property of the
	 *               user class
	 * @param device The device whose last action is to be explained
	 * @return Either the built explanation, or an error description in case the
	 *         explanation could not be built.
	 */
	@Override
	public String getExplanation(int min, String userId, String device) {
		// TODO Auto-generated method stub
		return null;
	}


}

/**
 * 
 */
package org.pfs.de.akismet;

import java.util.Collections;
import java.util.Map;

/**
 * Result of an Akismet spam check.
 * 
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class AkismetCheckResult {

	/**
	 * Possible result types.
	 * 
	 * @author Martin Dreier <martin@martindreier.de>
	 *
	 */
	public static enum ResultType {
		SPAM, HAM, INVALID
	}
	
	/**
	 * Additional information field: Akismet Server IP.
	 */
	public static final String INFO_SERVER = "X-akismet-server";
	
	/**
	 * Additional information field: Debug help.
	 */
	public static final String INFO_DEBUG = "X-akismet-debug-help";
	
	/**
	 * Additional information returned by the Akismet server.
	 */
	private Map<String, String> additionalInformation;
	
	/**
	 * Akismet check result.
	 */
	private ResultType result;

	/**
	 * Create a new check result.
	 * @param result The result of the check.
	 * @param additionalInformation Additional information from the Akismet server. 
	 */
	AkismetCheckResult(ResultType result, Map<String, String> additionalInformation) {
		this.additionalInformation = additionalInformation;
		this.result = result;
	}
	
	/**
	 * Create a new check result without additional information.
	 * @param result The result of the check.
	 */
	AkismetCheckResult(ResultType result) {
		this(result, Collections.<String, String>emptyMap());
	}

	/**
	 * Get additional information returned by the server.
	 * @return Additional information as key/value pairs. This map is immutable.
	 */
	public Map<String, String> getAdditionalInformation() {
		return Collections.unmodifiableMap(additionalInformation);
	}

	/**
	 * Check if the result is an error.
	 * @return <code>true</code> iff it is an error.
	 */
	public boolean isError() {
		return result == ResultType.INVALID;
	}
	
	/**
	 * Get the result of the Akismet check.
	 * @return the result
	 */
	public ResultType getResult() {
		return result;
	}
}

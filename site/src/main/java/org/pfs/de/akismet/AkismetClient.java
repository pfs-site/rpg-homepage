/**
 * 
 */
package org.pfs.de.akismet;

import org.pfs.de.akismet.AkismetCheckResult.ResultType;

/**
 * This is a client for the Akismet spam checking service.
 * 
 * <p>It is a facade for the REST API as exposed by Akismet.</p>
 * 
 * <p>See the <a href="https://akismet.com/development/api/">
 * Akismet API documentation</a> for more information of the API.</p>
 * 
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class AkismetClient {

	/**
	 * The Akismet API key.
	 */
	private String apiKey;

	/**
	 * Create a new Akismet client with the specified API key.
	 * @param apiKey
	 */
	public AkismetClient(String apiKey) {
		if (apiKey == null || apiKey.trim().equals("")) {
			throw new IllegalArgumentException("AKismet API key may not be null or empty");
		}
		this.apiKey = apiKey.trim();
	}
	
	/**
	 * Check if the API key is valid.
	 * @return The API key.
	 * @throws AkismetException
	 */
	public boolean checkApiKey() throws AkismetException {
		//TODO
		return true;
	}
	
	/**
	 * Check a comment for spam.
	 * @param commentData The comment data.
	 * @return <code>true</code> if comment is ham, <code>false</code> if comment is spam.
	 * @throws AkismetException
	 */
	public AkismetCheckResult checkComment(AkismetCommentData commentData) throws AkismetException {
		//TODO
		return new AkismetCheckResult(ResultType.HAM);
	}
	
	/**
	 * Report a comment as spam.
	 * @param commentData The comment data.
	 * @throws AkismetException
	 */
	public void reportSpam(AkismetCommentData commentData) throws AkismetException {
		//TODO
	}
	
	/**
	 * Report a comment as spam.
	 * @param commentData The comment data.
	 * @throws AkismetException
	 */
	public void reportHam(AkismetCommentData commentData) throws AkismetException {
		//TODO
	}
}

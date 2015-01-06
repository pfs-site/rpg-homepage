/**
 * 
 */
package org.pfs.de.akismet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.pfs.de.akismet.rest.AkismetApi;
import org.pfs.de.akismet.rest.AkismetRestClient;
import org.pfs.de.akismet.rest.AkismetUrls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * Log instance.
	 */
	private static final Logger log = LoggerFactory.getLogger(AkismetClient.class);

	/**
	 * The Akismet API key.
	 */
	private String apiKey;
	/**
	 * The URL of the homepage.
	 */
	private String homepage;
	
	/**
	 * The Akismet REST client used by this instance.
	 */
	private AkismetApi client;
	
	/**
	 * Cache keys verified by the API. This prevents additional calls to the API
	 * if the key has been previously verified. Keys where the verification failed are
	 * not saved in case the verification failed for temporary reasons (e.g. network
	 * issue, server downtime).
	 */
	private static Set<String> verifiedKeys = Collections.synchronizedSet(new HashSet<String>());
	
	/**
	 * This constructor is strictly intended to be used for testing. It allows
	 * injection of a test API implementing the {@link AkismetApi} interface.
	 * @param api The API instance.
	 * @param apiKey The API key.
	 * @param homepage Home page URL.
	 */
	AkismetClient(AkismetApi api, String apiKey, String homepage) {
		if (apiKey == null || apiKey.trim().equals("")) {
			throw new IllegalArgumentException("Akismet API key may not be null or empty");
		}
		if (homepage == null || homepage.trim().equals("")) {
			throw new IllegalArgumentException("Homepage may not be null or empty");
		}
		this.apiKey = apiKey.trim();
		this.homepage = homepage.trim();
		this.client = api;
	}
	
	/**
	 * Create a new Akismet client with the specified API key.
	 * @param apiKey
	 */
	public AkismetClient(String apiKey, String homepage) {
		this(new AkismetRestClient(), apiKey, homepage);
	}
	
	
	
	/**
	 * Check if the API key is valid. Additional information from the server is logged.
	 * @return <code>true</code> if the API key is valid, <code>false</code> otherwise.
	 * @throws AkismetException
	 */
	public boolean checkApiKey() throws AkismetException {
		if (verifiedKeys.contains(apiKey)) {
			//Previous check successful
			return true;
		}
		boolean checkResult = client.checkApiKey(apiKey, homepage);
		if (checkResult == true) {
			//Cache successful check result
			verifiedKeys.add(apiKey);
		}
		return checkResult;
	}
	
	/**
	 * Set the home page stored in this instance as the blog URL.
	 * @param commentData
	 */
	protected void setHomepage(AkismetCommentData commentData) {
		commentData.setBlogUrl(homepage);
	}
	
	/**
	 * Check a comment for spam.
	 * @param commentData The comment data.
	 * @return Result of the Akismet spam check.
	 * @throws AkismetException
	 */
	public AkismetCheckResult checkComment(AkismetCommentData commentData) throws AkismetException {
		setHomepage(commentData);
		AkismetCheckResult result = client.sendRequest(AkismetUrls.COMMENT_CHECK, commentData, apiKey);
		if (log.isDebugEnabled()) {
			log.debug("Check of comment {} returned result {}", commentData.getIdentifier(), result.getResult());
			if (!result.getAdditionalInformation().isEmpty()) {
				log.debug("Additional information for comment {}: {}", commentData.getIdentifier(), result.getAdditionalInformation());
			}
		}
		return result;
	}
	
	/**
	 * Report a comment as spam.
	 * @param commentData The comment data.
	 * @throws AkismetException
	 */
	public void reportSpam(AkismetCommentData commentData) throws AkismetException {
		setHomepage(commentData);
		AkismetCheckResult result = client.sendRequest(AkismetUrls.SUBMIT_SPAM, commentData, apiKey);
		if (log.isDebugEnabled()) {
			log.debug("Reporting comment {} as spam, result: {}", commentData.getIdentifier(), result.getResult());
		}
	}
	
	/**
	 * Report a comment as spam.
	 * @param commentData The comment data.
	 * @throws AkismetException
	 */
	public void reportHam(AkismetCommentData commentData) throws AkismetException {
		setHomepage(commentData);
		AkismetCheckResult result = client.sendRequest(AkismetUrls.SUBMIT_HAM, commentData, apiKey);
		if (log.isDebugEnabled()) {
			log.debug("Reporting comment {} as ham, result: {}", commentData.getIdentifier(), result.getResult());
		}
	}
}

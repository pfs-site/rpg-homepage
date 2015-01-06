package org.pfs.de.akismet.rest;

import org.pfs.de.akismet.AkismetCheckResult;
import org.pfs.de.akismet.AkismetCheckResult.ResultType;
import org.pfs.de.akismet.AkismetCommentData;
import org.pfs.de.akismet.AkismetException;

public interface AkismetApi {

	/**
	 * Send a request to Akismet.
	 * @param url The URL used.
	 * @param commentData Data of the comment.
	 * @param apiKey The API key.
	 * @return The check result. If the request if sent to a report URL (e.g. for
	 * spam or ham), the result is <em>always</em> {@link ResultType#INVALID}. This can
	 * be disregarded (the API does not return any information on these calls).
	 */
	public abstract AkismetCheckResult sendRequest(AkismetUrls url,
			AkismetCommentData commentData, String apiKey);

	/**
	 * Check if the API key is valid. Additional information from the server is logged.
	 * @param The API key.
	 * @param The homepage.
	 * @return <code>true</code> if the API key is valid, <code>false</code> otherwise.
	 * @throws AkismetException
	 */
	public abstract boolean checkApiKey(String apiKey, String homepage)
			throws AkismetException;

}
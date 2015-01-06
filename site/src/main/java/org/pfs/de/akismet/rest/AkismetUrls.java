package org.pfs.de.akismet.rest;

/**
 * URIs of the Akismet REST API.
 * 
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public enum AkismetUrls {
	VERIFY_KEY("https://rest.akismet.com/1.1/verify-key"),
	COMMENT_CHECK("https://{key}.rest.akismet.com/1.1/comment-check"),
	SUBMIT_SPAM("https://{key}.rest.akismet.com/1.1/submit-spam"),
	SUBMIT_HAM("https://{key}.rest.akismet.com/1.1/submit-ham");
	
	private String url;

	private AkismetUrls(String url) {
		this.url = url;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
}
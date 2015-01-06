package org.pfs.de.akismet.rest;

/**
 * This enum lists URL parameter names used in {@link AkismetUrls}. Use the {@link #toString()}
 * method to get the actual parameter name.
 * 
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
enum AkismetUrlParameters {
	API_KEY("key"),
	HOMEPAGE("blog"),
	USER_IP("user_ip"),
	USER_AGENT("user_agent"),
	REFERRER("referrer"),
	PERMALINK("permalink"),
	COMMENT_TYPE("comment_type"),
	AUTHOR_NAME("comment_author"),
	AUTHOR_EMAIL("comment_author_email"),
	AUTHOR_URL("comment_author_url"),
	CONTENT("comment_content"),
	COMMENT_DATE("comment_date_gmt"),
	MODIFIED_DATE("comment_post_modified_gmt"),
	LANGUAGE("blog_lang"),
	CHARSET("blog_charset");
	
	private String parameterName;

	private AkismetUrlParameters(String parameterName) {
		this.parameterName = parameterName;
	}
	
	@Override
	public String toString() {
		return parameterName;
	}
}
/**
 * 
 */
package org.pfs.de.akismet;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pfs.de.akismet.AkismetCheckResult.ResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

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
	 * URIs of the Akismet REST API.
	 * 
	 * @author Martin Dreier <martin@martindreier.de>
	 *
	 */
	protected enum AkismetUrls {
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

	/**
	 * This enum lists URL parameter names used in {@link AkismetUrls}. Use the {@link #toString()}
	 * method to get the actual parameter name.
	 * 
	 * @author Martin Dreier <martin@martindreier.de>
	 *
	 */
	protected enum AkismetUrlParameters {
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
	
	/**
	 * Possible replies from the Akismet REST API.
	 * 
	 * @author Martin Dreier <martin@martindreier.de>
	 *
	 */
	protected enum AkismetReply {
		VALID("valid"),
		INVALID("invalid"),
		TRUE("true"),
		FALSE("false");
		
		private String value;

		private AkismetReply(String value) {
			this.value = value;
		}
		
		/**
		 * Check if the given value equals this reply.
		 * @param value The value to test.
		 * @return <code>true</code> if the value is this reply, 
		 * <code>false</code> otherwise.
		 */
		public boolean is(String value) {
			if (value == null) {
				return false;
			}
			return this.value.equals(value.trim());
		}
	}
	
	/**
	 * Create a new Akismet client with the specified API key.
	 * @param apiKey
	 */
	public AkismetClient(String apiKey, String homepage) {
		if (apiKey == null || apiKey.trim().equals("")) {
			throw new IllegalArgumentException("Akismet API key may not be null or empty");
		}
		if (homepage == null || homepage.trim().equals("")) {
			throw new IllegalArgumentException("Homepage may not be null or empty");
		}
		this.apiKey = apiKey.trim();
		this.homepage = homepage.trim();
	}
	
	/**
	 * Encode data set to URL form encoding (<code>application/x-www-form-urlencoded</code>).
	 * @param data Data set.
	 * @return Encoded data.
	 */
	protected HttpEntity<String> encodeData(Map<String, String> data) {
		StringBuilder content = new StringBuilder();
		try {
			Iterator<Entry<String, String>> entries = data.entrySet().iterator();
			while (entries.hasNext()) {
				Entry<String, String> entry = entries.next();
				content.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()));
				content.append("=");
				content.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()));
				if (entries.hasNext()) {
					content.append("&");
				}
			}
		} catch (UnsupportedEncodingException e) {
			//This exception should not occur because a standard charset is used
			throw new RuntimeException(e);
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setContentLength(content.length());
		return new HttpEntity<String>(content.toString(), headers);
	}
	
	/**
	 * Check if the API key is valid. Additional information from the server is logged.
	 * @return <code>true</code> if the API key is valid, <code>false</code> otherwise.
	 * @throws AkismetException
	 */
	public boolean checkApiKey() throws AkismetException {
		RestTemplate template = new RestTemplate();
		ResponseEntity<String> response = template.postForEntity(AkismetUrls.VERIFY_KEY.getUrl(), encodeData(getDefaultRequestVariables()), String.class);
//		ResponseEntity<String> response = template.getForEntity(AkismetUrls.VERIFY_KEY.getUrl(), String.class, getDefaultRequestVariables());
		if (response.getStatusCode().equals(HttpStatus.OK)) {
			if (log.isDebugEnabled()) {
				log.debug("Akismet API key check returned: {} (debug help: {})", response.getBody(), getHeaderValue(response, AkismetCheckResult.INFO_DEBUG));
			}
			return AkismetReply.VALID.is(response.getBody());
		} else {
			log.error("Request to Aksimet REST API failed (server {})", getHeaderValue(response, AkismetCheckResult.INFO_SERVER));
		}
		return true;
	}
	
	/**
	 * Get the value of a named header.
	 * @param response The response object.
	 * @param headerName The header name.
	 * @return If the header is not available, it returns an empty string. Otherwise,
	 * returns the value of the header. If the header has multiple values, returns a
	 * list representation.
	 */
	protected String getHeaderValue(ResponseEntity<?> response, String headerName) {
		HttpHeaders headers = response.getHeaders();
		if (headers != null && headers.containsKey(headerName)) {
			List<String> values = headers.get(headerName);
			if (values == null || values.size() == 0) {
				//No entries
				return "";
			} else if (values.size() == 1) {
				//Only one value
				return values.get(0);
			} else {
				//Multiple values
				return values.toString();
			}
		} else {
			return "";
		}
	}
	
	/**
	 * Get a map with some request variables already set. The following variables
	 * are already contained in the map:
	 * <ul>
	 * <li>{@link AkismetUrlParameters#API_KEY}</li>
	 * <li>{@link AkismetUrlParameters#HOMEPAGE}</li>
	 * </ul>
	 */
	protected Map<String, String> getDefaultRequestVariables() {
		Map<String, String> variables = new HashMap<>();
		variables.put(AkismetUrlParameters.API_KEY.toString(), apiKey);
		variables.put(AkismetUrlParameters.HOMEPAGE.toString(), homepage);
		return variables;
	}
	
	/**
	 * Check a comment for spam.
	 * @param commentData The comment data.
	 * @return Result of the Akismet spam check.
	 * @throws AkismetException
	 */
	public AkismetCheckResult checkComment(AkismetCommentData commentData) throws AkismetException {
		//Build request data
		Map<String, String> variables = getDefaultRequestVariables();
		setVariableValues(variables, commentData);
		
		RestTemplate template = new RestTemplate();
		ResponseEntity<String> response = template.postForEntity(AkismetUrls.COMMENT_CHECK.getUrl(), encodeData(variables), String.class, apiKey);
		if (response.getStatusCode() == HttpStatus.OK) {
			if (AkismetReply.TRUE.is(response.getBody())) {
				//Spam
				return new AkismetCheckResult(ResultType.SPAM, getAdditionalInformation(response, AkismetCheckResult.INFO_SERVER, AkismetCheckResult.INFO_RECOMMENDATION));
			} else if (AkismetReply.FALSE.is(response.getBody())) {
				//Ham
				return new AkismetCheckResult(ResultType.HAM, getAdditionalInformation(response, AkismetCheckResult.INFO_SERVER));
			} else {
				//Error in processing
				return new AkismetCheckResult(ResultType.INVALID, getAdditionalInformation(response, AkismetCheckResult.INFO_SERVER, AkismetCheckResult.INFO_DEBUG));
			}
		} else {
			log.error("Request to Akismet REST API failed: with code {}", response.getStatusCode().value());
			return new AkismetCheckResult(ResultType.INVALID);
		}
	}
	
	/**
	 * Read additional information from the Akismet REST API response,
	 * @param response Response entity.
	 * @param headerNames Requested header names.
	 * @return Map of header names and values, if the requested header(s) are present in the
	 * response headers.
	 */
	protected Map<String, String> getAdditionalInformation(ResponseEntity<?> response, String... headerNames) {
		Map<String, String> additionalInfo = new HashMap<>();
		for (String information: headerNames) {
			if (response.getHeaders().containsKey(information)) {
				additionalInfo.put(information, getHeaderValue(response, information));
			}
		}
		return additionalInfo;
	}
	
	/**
	 * Copy data from object to map.
	 * @param variableData Map of variable data, will be filled with additional values.
	 * @param commentData Comment data object.
	 */
	protected void setVariableValues(Map<String, String> variableData, AkismetCommentData commentData) {
		//TODO: Implement more null checks on data
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-dd-MM'T'HH-mm-ss'Z'");
		setValueIfPresent(variableData, AkismetUrlParameters.AUTHOR_NAME, commentData.getAuthorName());
		setValueIfPresent(variableData, AkismetUrlParameters.AUTHOR_EMAIL, commentData.getAuthorName());
		setValueIfPresent(variableData, AkismetUrlParameters.AUTHOR_URL, commentData.getAuthorName());
		setValueIfPresent(variableData, AkismetUrlParameters.CHARSET, commentData.getCharset());
		//Default to current date
		setValueIfPresent(variableData, AkismetUrlParameters.COMMENT_DATE, dateFormat.format(commentData.getCommentDate() == null ? new Date() : commentData.getCommentDate()));
		setValueIfPresent(variableData, AkismetUrlParameters.COMMENT_TYPE, commentData.getCommentType().getType());
		setValueIfPresent(variableData, AkismetUrlParameters.CONTENT, commentData.getCommentContent());
		setValueIfPresent(variableData, AkismetUrlParameters.LANGUAGE, commentData.getLanguage());
		//Modified date is optional, set only if present
		setValueIfPresent(variableData, AkismetUrlParameters.MODIFIED_DATE, commentData.getDocumentDate() == null ? null : dateFormat.format(commentData.getDocumentDate()));
		setValueIfPresent(variableData, AkismetUrlParameters.PERMALINK, commentData.getPermalink());
		setValueIfPresent(variableData, AkismetUrlParameters.REFERRER, commentData.getReferrer());
		setValueIfPresent(variableData, AkismetUrlParameters.USER_AGENT, commentData.getUserAgent());
		setValueIfPresent(variableData, AkismetUrlParameters.USER_IP, commentData.getUserIp());
	}
	
	/**
	 * Set  a value in a map if the value is not empty. This method also ensures the value is properly
	 * encoded for transmission as a URL parameter. If the value is empty, adds a blank string as the 
	 * value to the map.
	 * @param variableData The target map.
	 * @param key The key.
	 * @param value The value. May be <code>null</code>.
	 */
	private void setValueIfPresent(Map<String, String> variableData, AkismetUrlParameters key, Object value) {
		if (value == null) {
			return;
		}
		try {
			String encoded = UriUtils.encodeQueryParam(value.toString(), StandardCharsets.UTF_8.name());
			variableData.put(key.toString(), encoded);
		} catch (UnsupportedEncodingException e) {
			log.error(MessageFormat.format("Cannot encode value {2} for key {1}", key, value), e);
		}
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

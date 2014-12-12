/**
 * 
 */
package org.pfs.de.akismet;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Locale;

/**
 * Data of a comment or post to be submitted to Akismet for spam checks.
 * 
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class AkismetCommentData {

	/**
	 * Identifier of the comment.
	 */
	private String identifier;
	
	/**
	 * The front page or home URL of the instance making the request. For a blog
	 * or wiki this would be the front page. Note: Must be a full URI, including
	 * http://. <br>
	 * <em>Required</em>
	 */
	private String blogUrl;

	/**
	 * IP address of the comment submitter. <br>
	 * <em>Required</em>
	 */
	private String userIp;

	/**
	 * User agent string of the web browser submitting the comment - typically
	 * the HTTP_USER_AGENT cgi variable. Not to be confused with the user agent
	 * of your Akismet library. <br>
	 * <em>Required</em>
	 */
	private String userAgent;

	/**
	 * The content of the HTTP_REFERER header should be sent here.
	 */
	private String referrer;

	/**
	 * The permanent location of the entry the comment was submitted to.
	 */
	private String permalink;

	/**
	 * May be blank, comment, trackback, pingback, or a made up value like
	 * "registration". It's important to send an appropriate value, and this is
	 * further explained here.
	 */
	private AkismetCommentType commentType;

	/**
	 * Name submitted with the comment.
	 */
	private String authorName;

	/**
	 * Email address submitted with the comment.
	 */
	private String authorEmail;

	/**
	 * URL submitted with comment.
	 */
	private String authorUrl;

	/**
	 * The content that was submitted.
	 */
	private String commentContent;

	/**
	 * The UTC timestamp of the creation of the comment, in ISO 8601 format. May
	 * be omitted if the comment is sent to the API at the time it is created.
	 */
	private Date commentDate = new Date();

	/**
	 * The UTC timestamp of the publication time for the post, page or thread on
	 * which the comment was posted.
	 */
	private Date documentDate;

	/**
	 * Indicates the language(s) in use on the blog or site, in ISO 639-1
	 * format, comma-separated. A site with articles in English and French might
	 * use "en, fr_ca".
	 */
	private Locale language = Locale.getDefault();

	/**
	 * The character encoding for the form values included in comment_*
	 * parameters, such as "UTF-8" or "ISO-8859-1".
	 */
	private Charset charset = Charset.defaultCharset();

	/**
	 * @return the blogUrl
	 */
	public String getBlogUrl() {
		return blogUrl;
	}

	/**
	 * @param blogUrl the blogUrl to set
	 */
	public void setBlogUrl(String blogUrl) {
		this.blogUrl = blogUrl;
	}

	/**
	 * @return the userIp
	 */
	public String getUserIp() {
		return userIp;
	}

	/**
	 * @param userIp the userIp to set
	 */
	public void setUserIp(String userIp) {
		this.userIp = userIp;
	}

	/**
	 * @return the userAgent
	 */
	public String getUserAgent() {
		return userAgent;
	}

	/**
	 * @param userAgent the userAgent to set
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * @return the referrer
	 */
	public String getReferrer() {
		return referrer;
	}

	/**
	 * @param referrer the referrer to set
	 */
	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}

	/**
	 * @return the permalink
	 */
	public String getPermalink() {
		return permalink;
	}

	/**
	 * @param permalink the permalink to set
	 */
	public void setPermalink(String permalink) {
		this.permalink = permalink;
	}

	/**
	 * @return the commentType
	 */
	public AkismetCommentType getCommentType() {
		return commentType;
	}

	/**
	 * @param commentType the commentType to set
	 */
	public void setCommentType(AkismetCommentType commentType) {
		this.commentType = commentType;
	}

	/**
	 * @return the authorName
	 */
	public String getAuthorName() {
		return authorName;
	}

	/**
	 * @param authorName the authorName to set
	 */
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	/**
	 * @return the authorEmail
	 */
	public String getAuthorEmail() {
		return authorEmail;
	}

	/**
	 * @param authorEmail the authorEmail to set
	 */
	public void setAuthorEmail(String authorEmail) {
		this.authorEmail = authorEmail;
	}

	/**
	 * @return the authorUrl
	 */
	public String getAuthorUrl() {
		return authorUrl;
	}

	/**
	 * @param authorUrl the authorUrl to set
	 */
	public void setAuthorUrl(String authorUrl) {
		this.authorUrl = authorUrl;
	}

	/**
	 * @return the commentContent
	 */
	public String getCommentContent() {
		return commentContent;
	}

	/**
	 * @param commentContent the commentContent to set
	 */
	public void setCommentContent(String commentContent) {
		this.commentContent = commentContent;
	}

	/**
	 * @return the commentDate
	 */
	public Date getCommentDate() {
		return commentDate;
	}

	/**
	 * @param commentDate the commentDate to set
	 */
	public void setCommentDate(Date commentDate) {
		this.commentDate = commentDate;
	}

	/**
	 * @return the documentDate
	 */
	public Date getDocumentDate() {
		return documentDate;
	}

	/**
	 * @param documentDate the documentDate to set
	 */
	public void setDocumentDate(Date documentDate) {
		this.documentDate = documentDate;
	}

	/**
	 * @return the language
	 */
	public Locale getLanguage() {
		return language;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(Locale language) {
		this.language = language;
	}

	/**
	 * @return the charset
	 */
	public Charset getCharset() {
		return charset;
	}

	/**
	 * @param charset the charset to set
	 */
	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
}

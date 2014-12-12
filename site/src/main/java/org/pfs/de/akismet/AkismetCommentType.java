/**
 * 
 */
package org.pfs.de.akismet;

/**
 * Comment types understood by Akismet.
 * 
 * <p>See
 * <a href="http://blog.akismet.com/2012/06/19/pro-tip-tell-us-your-comment_type/">
 * http://blog.akismet.com/2012/06/19/pro-tip-tell-us-your-comment_type/</a>
 * for information about comment types.</p>
 * 
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public enum AkismetCommentType {
	
	/**
	 * For blog comment forms.
	 */
	COMMENT ("comment"),
	/**
	 * Pingbacks.
	 */
    PINGBACK ("pingback"),
    /**
     * Trackbacks.
     */
    TRACKBACK ("trackback"),
    /**
     * Forum posts and replies.
     */
    FORUM_POST("forum-post"),
    /**
     * Blog posts.
     */
    BLOG_POST ("blog-post"),
    /**
     * Contact forms, inquiry forms and the like.
     */
    CONTACT_FORM ("contact-form"),
    /**
     * Account signup, registration or activation.
     */
    SIGNUP ("signup"),
    /**
     * Twitter messages
     */
    TWEET ("tweet");
    
	/**
	 * Comment type.
	 */
	private String type;

	/**
	 * Create a new comment type.
	 * @param type The type identified, as expected by Akismet.
	 */
	private AkismetCommentType(String type) {
		this.type = type;
	}

	/**
	 * Get the comment type.
	 * @return
	 */
	String getType() {
		return type;
	}
}

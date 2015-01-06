/**
 * 
 */
package org.pfs.de.akismet;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * JCR mixin node type: Akismet check result.
	 */
	private static final String NODE_TYPE_CHECK_RESULT = "website:akismetcheckresult";
	
	/**
	 * JCR node property: Akismet check result.
	 */
	private static final String NODE_PROEPRTY_CHECK_RESULT = "website:akismetCheckResult";
	
	/**
	 * Log instance.
	 */
	private static final Logger log = LoggerFactory.getLogger(AkismetCheckResult.class);
	
	/**
	 * Additional information field: Akismet Server IP.
	 */
	public static final String INFO_SERVER = "X-akismet-server";
	
	/**
	 * Additional information field: Debug help.
	 */
	public static final String INFO_DEBUG = "X-akismet-debug-help";
	
	/**
	 * Additional information field: Recommended action.
	 */
	public static final String INFO_RECOMMENDATION = "X-akismet-pro-tip";
	
	/**
	 * The field {@link #INFO_RECOMMENDATION} is set to this value if the
	 * comment should be directly discarded.
	 */
	public static final String INFO_RECOMMENDATION_DISCARD = "discard";
	
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
	public AkismetCheckResult(ResultType result, Map<String, String> additionalInformation) {
		this.additionalInformation = additionalInformation;
		this.result = result;
	}
	
	/**
	 * Create a new check result without additional information.
	 * @param result The result of the check.
	 */
	public AkismetCheckResult(ResultType result) {
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
	
	/**
	 * Save the check result to a JCR node.
	 * @param targetNode The target node.
	 * @return <code>true</code> if the node was updated successfully, 
	 * <code>false</code> if an error occurred. Error information is 
	 * written to the log.
	 */
	public boolean save(Node targetNode) {
		if (targetNode == null) {
			log.error("Node is null");
			return false;
		}
		try {
			if (!targetNode.isNodeType(NODE_TYPE_CHECK_RESULT)) {
				targetNode.addMixin(NODE_TYPE_CHECK_RESULT);
			}
		} catch (RepositoryException e) {
			try {
				log.error(MessageFormat.format("Cannot add Akismet Check Result node type to node {}", targetNode.getIdentifier()));
			} catch (RepositoryException e1) {
				log.error("Error reporting error", e);
			}
			return false;
		}
		try {
			targetNode.setProperty(NODE_PROEPRTY_CHECK_RESULT, result.name().toLowerCase());
			return true;
		} catch (RepositoryException e) {
			try {
				log.error(MessageFormat.format("Cannot store Akismet Check Result on node {}", targetNode.getIdentifier()));
			} catch (RepositoryException e1) {
				log.error("Error reporting error", e);
			}
			return false;
		}
	}
}

package org.pfs.de.akismet;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Data holder class for Akismet configuration.
 * 
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class AkismetConfiguration {
	
	/**
	 * Node type: Configuration for Akismet publication.
	 */
	public static final String NT_AKISMET_CHECK = "website:akismetcheck";
	
	/**
	 * Property name: Akismet API Key.
	 */
	public static final String PROP_AKISMET_KEY = "website:akismetApiKey";
	
	/**
	 * Property name: Akismet API Key.
	 */
	public static final String PROP_AKISMET_HAM_ACTION = "website:akismetHamAction";
	
	/**
	 * Property name: Akismet API Key.
	 */
	public static final String PROP_AKISMET_SPAM_ACTION = "website:akismetSpamAction";

	/**
	 * Property value: Reject spam comments.
	 */
	public static final String PROP_VALUE_SPAM_ACTION_REJECT = "reject";

	
	/**
	 * Akismet API key.
	 */
	public String apiKey;
	
	/**
	 * Action on akismet check with ham result.
	 */
	public String hamAction;
	
	/**
	 * Action on akismet check with spam result.
	 */
	public String spamAction;
	
	/**
	 * Check if configuration is complete. Configuration is complete if:
	 * <ol>
	 *   <li>API key is set and not empty</li>
	 *   <li>Action is set</li>
	 * </ol> 
	 * @return
	 */
	public boolean isComplete() {
		return apiKey != null && apiKey.length() > 0 && hamAction != null && hamAction.length() > 0;
	}
	
	/**
	 * Read AKismet configuration from a node. Reads the current node and its
	 * parent nodes, up to the root node.
	 * 
	 * @param node The first node.
	 * @return The configuration found on the nodes.
	 * @throws RepositoryException Error reading data from repository.
	 */
	public static AkismetConfiguration readConfiguration(Session session, Node node) throws RepositoryException {
		Node currentNode = node;
		Node rootNode = session.getRootNode();
		AkismetConfiguration configuration = new AkismetConfiguration();
		while(true) {
			//If current node is Akismet configuration node, read configuration data
			if (currentNode.isNodeType(NT_AKISMET_CHECK)) {
				if (configuration.apiKey == null && currentNode.hasProperty(PROP_AKISMET_KEY)) {
					configuration.apiKey = currentNode.getProperty(PROP_AKISMET_KEY).getString().trim();
				}
				if (configuration.hamAction == null && currentNode.hasProperty(PROP_AKISMET_HAM_ACTION)) {
					configuration.hamAction = currentNode.getProperty(PROP_AKISMET_HAM_ACTION).getString().trim();
				}
				if (configuration.spamAction == null && currentNode.hasProperty(PROP_AKISMET_SPAM_ACTION)) {
					configuration.spamAction = currentNode.getProperty(PROP_AKISMET_SPAM_ACTION).getString().trim();
				}
			}
			//End processing once configuration is complete. Parent nodes will not overwrite
			// current values, therefore it is not necessary to continue
			if (configuration.isComplete()) {
				break;
			}
			if (rootNode.isSame(currentNode)) {
				//Reached root node, end processing
				break;
			}
			//Move to parent node
			currentNode = currentNode.getParent();
		}
		return configuration;
	}
}
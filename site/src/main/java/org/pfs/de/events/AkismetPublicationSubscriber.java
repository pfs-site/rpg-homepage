/**
 * 
 */
package org.pfs.de.events;

import java.rmi.RemoteException;
import java.util.Arrays;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.hippoecm.hst.content.beans.ObjectBeanManagerException;
import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.api.HippoWorkspace;
import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.reviewedactions.FullReviewedActionsWorkflow;
import org.onehippo.forge.repositoryeventlistener.hst.events.BaseHippoEventSubscriber;
import org.onehippo.forge.repositoryeventlistener.hst.hippo.EventType;
import org.onehippo.forge.repositoryeventlistener.hst.hippo.HippoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event subscriber for automatic spam check and publishing.
 * 
 * <p>This subscriber requires the 
 * <a href="http://repo_event_list.forge.onehippo.org">Repository Event Listener</a>
 * plugin for Hippo. The JCR user for the event handler must have the following
 * permissions for nodes on which an action is defined:
 * <ul>
 *   <li>Read nodes (up to root node)</li>
 *   <li>Add mixin node type</li>
 *   <li>Set properties on nodes</li>
 *   <li>Publish nodes</li>
 *   <li>Send publication requests</li>
 * </ul>
 * 
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class AkismetPublicationSubscriber extends BaseHippoEventSubscriber {

	/**
	 * Node type: Configuration for Akismet publication.
	 */
	public static final String NT_AKISMET_PUBLISH = "website:akismetpublish";
	
	/**
	 * Property name: Akismet API Key.
	 */
	public static final String PROP_AKISMET_KEY = "website:akismetApiKey";
	
	/**
	 * Property name: Akismet API Key.
	 */
	public static final String PROP_AKISMET_ACTION = "website:akismetAction";
	
	/**
	 * Enum constants for possible actions. The {@link String} values must be
	 * used as the attribute value for the node property 
	 * {@link AkismetPublicationSubscriber#PROP_AKISMET_ACTION}.
	 * 
	 * @author Martin Dreier <martin@martindreier.de>
	 *
	 */
	public static enum AkismetAction {
		IGNORE("ignore"), PUBLISH("publish"), PUBLICATION_REQUEST("request");
		
		/**
		 * Value of the action. This is the value of the node property 
		 * {@link AkismetPublicationSubscriber#PROP_AKISMET_ACTION}.
		 */
		private String value;

		/**
		 * Create a new Akismet action definition.
		 * @param value The action value.
		 * @see #value
		 */
		private AkismetAction(String value) {
			this.value = value;
		}
		
		/**
		 * Get the enum value from the text value.
		 * @param value The action value.
		 * @return The enum, or <code>null</code> if no enum 
		 * corresponds to the text value.
		 */
		public static AkismetAction getAction(String value) {
			for (AkismetAction action: values()) {
				if (action.value.equals(value)) {
					return action;
				}
			}
			return null;
		}
	}
	
	/**
	 * Logger instance.
	 */
	private static final Logger log = LoggerFactory.getLogger(AkismetPublicationSubscriber.class);
	
	/**
	 * @see org.onehippo.forge.repositoryeventlistener.hst.hippo.HippoEventSubscriber#getName()
	 */
	@Override
	public String getName() {
		return AkismetPublicationSubscriber.class.getName();
	}

	/**
	 * @see org.onehippo.forge.repositoryeventlistener.hst.events.BaseHippoEventSubscriber#onEvent(org.onehippo.forge.repositoryeventlistener.hst.hippo.HippoEvent)
	 */
	@Override
	public void onEvent(HippoEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("Handling event type {} ({}) for node {} ({})", event.getType().name(), event.getType().getType(), event.getIdentifier(), event.getPath());
		}
		try {
			boolean success = false;
			Node eventNode = getSession().getNodeByIdentifier(event.getIdentifier());
			if (eventNode == null) {
				log.error("Node {} not found in current session", event.getIdentifier());
				return;
			}
			
			if (event.getType().equals(EventType.UNPUBLISHED)) {
				//Ignore unpublished nodes in future
				if (log.isDebugEnabled()) {
					log.debug("Node {} is unpublished, setting Akismet action to ignore", eventNode.getIdentifier());
				}
				ignoreNode(eventNode);
			} else if (isDocumentNew(event)) {
				//Document created or changed
				AkismetConfiguration configuration = readConfiguration(eventNode);
				if (!configuration.isComplete()) {
					//Configuration incomplete, cancel processing
					log.warn("Reading configuration for node {} returned incomplete configuration", eventNode.getIdentifier());
					return;
				}
				if (configuration.action.equals(AkismetAction.IGNORE)) {
					//Node shall be ignored
					if (log.isDebugEnabled()) {
						log.debug("Node {} is set to ignore", eventNode.getIdentifier());
					}
					return;
				}

				//Check if node should be published
				boolean publish = isPublishable(eventNode, configuration);
				if (!publish) {
					log.debug("Node {} not publishable", eventNode.getIdentifier());
					return;
				}
				
				Node nodeToPublish = null;
				if (eventNode.isNodeType(HippoNodeType.NT_HANDLE)) {
					nodeToPublish = findUnpublishedDocumentVersion(eventNode);
				} else {
					//New node is not a handle
					log.debug("Document creation event recieved, but new node is not a document handle ({})", eventNode.getPrimaryNodeType().getName());
				}
				
				//Check that publishable node was found
				if (nodeToPublish == null) {
					log.debug("Found no publishable document");
					return;
				}
				if (log.isDebugEnabled()) {
					log.debug("Attempting to publish node {} ({}))", nodeToPublish.getIdentifier(), nodeToPublish.getPath());
				}
				success = publishDocument(nodeToPublish, configuration.action);
			}
			//If node change was successful (set to ignore, publish, or publication request)
			// save changes in session.
			if (success) {
				getSession().save();
			}
		} catch (RepositoryException | RemoteException | WorkflowException | ObjectBeanManagerException e) {
			log.error("Publication of document failed", e);
		}
	}

	/**
	 * Check node content for spam.
	 * @param node The node to check.
	 * @param configuration Akismet configuration.
	 * @return <code>true</code> if node is clean, <code>false</code> if identified as spam or
	 * other reason it should not be published.
	 * @throws RepositoryException 
	 * @throws ObjectBeanManagerException 
	 */
	protected boolean isPublishable(Node node, AkismetConfiguration configuration) throws ObjectBeanManagerException, RepositoryException {
		//TODO: Implement
		return false;
	}
	
	/**
	 * Set Akismet action on a node to ignore to ensure it is not
	 * automatically published later.
	 * 
	 * @param documentHandle The node to ignore.
	 * 
	 * @return <code>true</code> if ignore action succeeded, <code>false</code> on failure. Log
	 * contains information of reason for failure.
	 * 
	 * @throws RepositoryException General error.
	 * @throws LockException Cannot acquire lock on node.
	 * @throws ConstraintViolationException Akismet mixin conflicts with current node state.
	 * @throws VersionException Cannot create new node version.
	 * @throws NoSuchNodeTypeException Mixin type is not known.
	 */
	protected boolean ignoreNode(Node documentHandle) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
		if (documentHandle.canAddMixin(NT_AKISMET_PUBLISH)) {
			documentHandle.addMixin(NT_AKISMET_PUBLISH);
			documentHandle.setProperty(PROP_AKISMET_ACTION, AkismetAction.IGNORE.value);
			return true;
		} else {
			log.warn("Cannot add mixing {} to node {}", NT_AKISMET_PUBLISH, documentHandle.getIdentifier());
			return false;
		}
	}
	
	/**
	 * Find the unpublished version of a document node.
	 * @param documentHandle The node which is the document handle.
	 * @return The node of the unpublished version, or <code>null</code> if
	 * no unpublished version exists of this document.
	 * @throws RepositoryException
	 */
	protected Node findUnpublishedDocumentVersion(Node documentHandle) throws RepositoryException {
		NodeIterator children = documentHandle.getNodes();
		while (children.hasNext()) {
			Node documentNode = (Node) children.next();
			//Find unpublished state
			if (documentNode.isNodeType("hippostdpubwf:document") && documentNode.hasProperty("hippostd:state") && documentNode.getProperty("hippostd:state").getString().equals("unpublished")) {
				return documentNode;
			}
		}
		return null;
	}
	
	/**
	 * Publish a document.
	 * @param documentNode The node of the document to publish.
	 * @return <code>true</code> if publication was successful, <code>false</code> if it fails. Reason
	 * for failure will be written into the log.
	 * @throws RepositoryException
	 * @throws RemoteException Publication failed.
	 * @throws WorkflowException Error reading workflow.
	 */
	protected boolean publishDocument(Node documentNode, AkismetAction action) throws RepositoryException, RemoteException, WorkflowException {
		if (action.equals(AkismetAction.IGNORE)) {
			log.warn("Node {} set to ignore, cannot publish", documentNode.getIdentifier());
			return false;
		}
		//Find correct workflow for publishing
		Workflow workflow = ((HippoWorkspace)getSession().getWorkspace()).getWorkflowManager().getWorkflow("default", documentNode);
		if (workflow == null) {
			log.warn("Found no workflow for category {} and node {} ({})", "default", documentNode.getPath(), documentNode.getIdentifier());
			return false;
		}
		
		if (workflow instanceof FullReviewedActionsWorkflow) {
			//Publish document or send publication request
			if (AkismetAction.PUBLISH.equals(action)) {
				((FullReviewedActionsWorkflow)workflow).publish();
			} else if (AkismetAction.PUBLICATION_REQUEST.equals(action)) {
				((FullReviewedActionsWorkflow)workflow).requestPublication();
			} else {
				log.error("Unexpected Akismet action requested: {}", action.value);
			}
		} else {
			log.error("Workflow has wrong interface type: {}", Arrays.asList(workflow.getClass().getInterfaces()));
			return false;
		}
		return true;
	}
	
	/**
	 * Read AKismet configuration from a node. Reads the current node and its
	 * parent nodes, up to the root node.
	 * 
	 * @param node The first node.
	 * @return The configuration found on the nodes.
	 * @throws RepositoryException Error reading data from repository.
	 */
	protected AkismetConfiguration readConfiguration(Node node) throws RepositoryException {
		Node currentNode = node;
		Node rootNode = getSession().getRootNode();
		AkismetConfiguration configuration = new AkismetConfiguration();
		while(true) {
			//If current node is Akismet configuration node, read configuration data
			if (currentNode.isNodeType(NT_AKISMET_PUBLISH)) {
				if (configuration.action == null && currentNode.hasProperty(PROP_AKISMET_ACTION)) {
					configuration.action = AkismetAction.getAction(currentNode.getProperty(PROP_AKISMET_ACTION).getString().trim());
				}
				if (configuration.apiKey == null && currentNode.hasProperty(PROP_AKISMET_KEY)) {
					configuration.apiKey = currentNode.getProperty(PROP_AKISMET_KEY).getString().trim();
				}
			}
			//End processing once configuration is complete. Parent nodes will not overwrite
			// current values, therefor it is not necessary to continue
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
	
	/**
	 * Data holder class for Akismet configuration.
	 * 
	 * @author Martin Dreier <martin@martindreier.de>
	 *
	 */
	private class AkismetConfiguration {
		/**
		 * Akismet API key.
		 */
		public String apiKey;
		/**
		 * Akismet action.
		 */
		public AkismetAction action;
		
		/**
		 * Check if configuration is complete. Configuration is complete if:
		 * <ol>
		 *   <li>API key is set and not empty</li>
		 *   <li>Action is set</li>
		 * </ol> 
		 * @return
		 */
		public boolean isComplete() {
			return apiKey != null && apiKey.length() > 0 && action != null;
		}
	}
}

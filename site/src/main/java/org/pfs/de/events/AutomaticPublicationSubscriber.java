/**
 * 
 */
package org.pfs.de.events;

import java.rmi.RemoteException;
import java.util.Arrays;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

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
 * Event subscriber for automatic publishing.
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
public class AutomaticPublicationSubscriber extends BaseHippoEventSubscriber {

	/**
	 * Node type for automatic publishing configuration.
	 */
	public static final String NT_WEBSITE_AUTOPUBLISH = "website:autopublish";
	
	/**
	 * Property name: Automatic publishin action. See {@link PublishAction}
	 * for allowed values.
	 */
	public static final String PROP_PUBLISH_ACTION = "website:publishAction";
	
	/**
	 * Enum constants for possible actions. The {@link String} values must be
	 * used as the attribute value for the node property 
	 * {@link AutomaticPublicationSubscriber#PROP_PUBLISH_ACTION}.
	 * 
	 * @author Martin Dreier <martin@martindreier.de>
	 *
	 */
	public static enum PublishAction {
		IGNORE("ignore"), PUBLISH("publish"), PUBLICATION_REQUEST("request");
		
		/**
		 * Value of the action. This is the value of the node property 
		 * {@link AutomaticPublicationSubscriber#PROP_PUBLISH_ACTION}.
		 */
		private String value;

		/**
		 * Create a new Akismet action definition.
		 * @param value The action value.
		 * @see #value
		 */
		private PublishAction(String value) {
			this.value = value;
		}
		
		/**
		 * Get the enum value from the text value.
		 * @param value The action value.
		 * @return The enum, or <code>null</code> if no enum 
		 * corresponds to the text value.
		 */
		public static PublishAction getAction(String value) {
			for (PublishAction action: values()) {
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
	private static final Logger log = LoggerFactory.getLogger(AutomaticPublicationSubscriber.class);
	
	/**
	 * @see org.onehippo.forge.repositoryeventlistener.hst.hippo.HippoEventSubscriber#getName()
	 */
	@Override
	public String getName() {
		return AutomaticPublicationSubscriber.class.getName();
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
				PublishAction action = getPublishAction(eventNode);
				if (action == null) {
					//Configuration incomplete, cancel processing
					log.warn("Reading configuration for node {} returned no publish action", eventNode.getIdentifier());
					return;
				}
				if (action.equals(PublishAction.IGNORE)) {
					//Node shall be ignored
					if (log.isDebugEnabled()) {
						log.debug("Node {} is set to ignore", eventNode.getIdentifier());
					}
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
				success = publishDocument(nodeToPublish, action);
			}
			//If node change was successful (set to ignore, publish, or publication request)
			// save changes in session.
			if (success) {
				getSession().save();
			}
		} catch (RepositoryException | RemoteException | WorkflowException e) {
			log.error("Publication of document failed", e);
		}
	}
	
	/**
	 * Get the publish action configured on the <code>node</code>. Searches the given node and
	 * all parent nodes up to the root node.
	 * @param node The node.
	 * @return The configured publish action, or <code>null</code> if no action was configured.
	 * @throws RepositoryException 
	 */
	protected PublishAction getPublishAction(Node node) throws RepositoryException {
		Node currentNode = node;
		Node rootNode = getSession().getRootNode();
		PublishAction action = null;
		while(action == null) {
			//If current node is Akismet configuration node, read configuration data
			if (currentNode.isNodeType(NT_WEBSITE_AUTOPUBLISH)) {
				if (currentNode.hasProperty(PROP_PUBLISH_ACTION)) {
					String actionValue = currentNode.getProperty(PROP_PUBLISH_ACTION).getString().trim();
					action = PublishAction.getAction(actionValue);
					if (action == null) {
						log.warn("Unknown publish action {} found on node {} ({})", actionValue, node.getIdentifier(), node.getPath());
					}
				}
			}
			if (rootNode.isSame(currentNode)) {
				//Reached root node, end processing
				break;
			}
			//Move to parent node
			currentNode = currentNode.getParent();
		}
		return action;
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
	 */
	protected boolean ignoreNode(Node documentHandle) throws RepositoryException {
		return AutomaticPublicationSubscriber.setAutoPublishAction(documentHandle, PublishAction.IGNORE);
	}
	
	/**
	 * Set the publishing action on a node.
	 * @param node The node.
	 * @param action The desired action.
	 * @return <code>true</code> if setting the action succeeded, <code>false</code> if it fails. Error
	 * reasons will be written to the log.
	 * @throws RepositoryException
	 */
	public static boolean setAutoPublishAction(Node node, PublishAction action) throws RepositoryException {
		if (log.isDebugEnabled()) {
			log.debug("Setting action {} on node {} ({})", action.value, node.getIdentifier(), node.getPath());
		}
		//Check that node has required mixing and add if necessary
		if (!node.isNodeType(NT_WEBSITE_AUTOPUBLISH)) {
			if (node.canAddMixin(NT_WEBSITE_AUTOPUBLISH)) {
				node.addMixin(NT_WEBSITE_AUTOPUBLISH);
			} else {
				log.error("Cannot add mixin {} to node {}", NT_WEBSITE_AUTOPUBLISH, node.getIdentifier());
				return false;
			}
		}
		//Set property value
		Property property = node.setProperty(PROP_PUBLISH_ACTION, action.value);
		if (property != null && property.getString().equals(action.value)) {
			return true;
		} else {
			log.error("Setting action {} on node {} failed, current value {}", action.value, node.getIdentifier(), property == null ? "<null>" : property.getString());
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
	protected boolean publishDocument(Node documentNode, PublishAction action) throws RepositoryException, RemoteException, WorkflowException {
		if (action.equals(PublishAction.IGNORE)) {
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
			if (PublishAction.PUBLISH.equals(action)) {
				((FullReviewedActionsWorkflow)workflow).publish();
			} else if (PublishAction.PUBLICATION_REQUEST.equals(action)) {
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
	
	
}

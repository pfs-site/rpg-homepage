/**
 * 
 */
package org.pfs.de.events;

import java.rmi.RemoteException;
import java.util.Arrays;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.api.HippoWorkspace;
import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.reviewedactions.FullReviewedActionsWorkflow;
import org.onehippo.forge.repositoryeventlistener.hst.events.BaseHippoEventSubscriber;
import org.onehippo.forge.repositoryeventlistener.hst.hippo.HippoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event subscriber for automatic spam check and publishing.
 * 
 * <p>This subscriber requires the 
 * <a href="http://repo_event_list.forge.onehippo.org">Repository Event Listener</a>
 * plugin for Hippo.
 * 
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class AkismetPublicationSubscriber extends BaseHippoEventSubscriber {

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
		//TODO: Akismet spam check
		//TODO: Handle manually depublished nodes (block automatic publishing)
		if (log.isDebugEnabled()) {
			log.debug("Handling event type {} ({}) for node {} ({})", event.getType().name(), event.getType().getType(), event.getIdentifier(), event.getPath());
		}
		try {
			if (isDocumentNew(event)) {
				//Document created or changed
				Node newNode = getSession().getNodeByIdentifier(event.getIdentifier());
				Node nodeToPublish = null;
				
				if (newNode.isNodeType(HippoNodeType.NT_HANDLE)) {
					//Parent node found, but child document must be published
					NodeIterator children = newNode.getNodes();
					while (children.hasNext()) {
						Node documentNode = (Node) children.next();
						//Find unpublished state
						if (documentNode.isNodeType("hippostdpubwf:document") && documentNode.hasProperty("hippostd:state") && documentNode.getProperty("hippostd:state").getString().equals("unpublished")) {
							nodeToPublish = documentNode;
						}
					}
				} else {
					//New node is not a handle
					log.debug("Document creation event recieved, but new node is not a document handle ({})", newNode.getPrimaryNodeType().getName());
				}
				
				//Check that publishable node was found
				if (nodeToPublish == null) {
					log.debug("Found no publishable document");
					return;
				}
				if (log.isDebugEnabled()) {
					log.debug("Attempting to publish node {} ({}))", nodeToPublish.getIdentifier(), nodeToPublish.getPath());
				}
				
				//Find correct workflow for publishing
				Workflow workflow = ((HippoWorkspace)getSession().getWorkspace()).getWorkflowManager().getWorkflow("default", nodeToPublish);
				if (workflow == null) {
					log.warn("Found no workflow for category {} and node {} ({})", "default", nodeToPublish.getPath(), nodeToPublish.getIdentifier());
					return;
				}
				
				if (workflow instanceof FullReviewedActionsWorkflow) {
					//Publish document and save changes
					((FullReviewedActionsWorkflow)workflow).publish();
					getSession().save();
				} else {
					log.error("Workflow has wrong interface type: {}", Arrays.asList(workflow.getClass().getInterfaces()));
					return;
				}
			}
			
		} catch (RepositoryException | RemoteException | WorkflowException e) {
			log.error("Publication of document failed", e);
		}
	}

}

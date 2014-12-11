package org.pfs.de.workflow.events;

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.hippoecm.repository.api.Document;
import org.hippoecm.repository.api.HippoWorkspace;
import org.hippoecm.repository.api.MappingException;
import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.api.WorkflowManager;
import org.hippoecm.repository.ext.WorkflowImpl;
import org.hippoecm.repository.reviewedactions.FullReviewedActionsWorkflow;
import org.hippoecm.repository.standardworkflow.WorkflowEventWorkflow;

/**
 * Workflow event to handle addition of new comments. 
 * 
 * Checks comment for spam using Akismet.
 * 
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
@PersistenceCapable(identityType=IdentityType.DATASTORE,cacheable="false", detachable="false")
@DatastoreIdentity(strategy=IdGeneratorStrategy.NATIVE)
@Inheritance(strategy=InheritanceStrategy.SUBCLASS_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public class AddComment extends WorkflowImpl implements WorkflowEventWorkflow {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = -3918920064072890716L;

	private static final String PUBLICATION_WORKFLOW = "publish";
	
	public AddComment() throws RemoteException {
		super();
	}

	/**
	 * This method is not supported for this event.
	 */
	@Override
	public void fire() throws WorkflowException, MappingException,
			RepositoryException, RemoteException {
		throw new WorkflowException("Unexpected call to method fire()");
	}

	/**
	 * @see WorkflowEventWorkflow#fire(Document)
	 */
	@Override
	public void fire(Document document) throws WorkflowException,
			MappingException, RepositoryException, RemoteException {
		this.publish(document);
	}

	/**
	 * Publish a document.
	 * @param document The document to publish.
	 * @throws RepositoryException
	 * @throws RemoteException
	 * @throws WorkflowException
	 */
	protected void publish(Document document) throws RepositoryException, RemoteException, WorkflowException {
		//Get the node associated with the document
		Node node = getWorkflowContext().getUserSession().getNodeByIdentifier(document.getIdentity());
		if (node == null) {
			throw new WorkflowException(String.format("Node with identity %s not found", document.getIdentity()));
		}

		//Get the publication workflow and commit the edited document
		FullReviewedActionsWorkflow publishWorkflow = getWorkflow(FullReviewedActionsWorkflow.class, PUBLICATION_WORKFLOW, node);
		publishWorkflow.commitEditableInstance();
		
		//Re-read the workflow to get the current state, then publish the document
		publishWorkflow = getWorkflow(FullReviewedActionsWorkflow.class, PUBLICATION_WORKFLOW, node);
		publishWorkflow.publish();
		publishWorkflow.obtainEditableInstance();
	}
	
	/**
	 * Get a workflow of the specified category.
	 * @param workflowClass The interface (or class) of the returned workflow.
	 * @param category The workflow category.
	 * @param node The node for which the workflow is retrieved.
	 * @return The found workflow.
	 * @throws RepositoryException Workflow manager or workflow cannot be found.
	 * @throws WorkflowException The returned workflow has the wrong class.
	 */
	@SuppressWarnings("unchecked")
	private <T extends Workflow> T getWorkflow(Class<T> workflowClass, String category, Node node) throws RepositoryException, WorkflowException {
		WorkflowManager wflManager = ((HippoWorkspace) getWorkflowContext().getUserSession().getWorkspace()).getWorkflowManager();
		Workflow workflow = wflManager.getWorkflow(category, node);
		if (workflowClass.isAssignableFrom(workflow.getClass())) {
			return (T) workflow;
		} else {
			throw new WorkflowException(MessageFormat.format("Workflow for node {} and category {} has wrong class (expected: {}, actual: {})", node.getIdentifier(), category, workflowClass.getName(), workflow.getClass().getName()));
		}
	}
	
	/**
	 * @see WorkflowEventWorkflow#fire(Iterator)
	 */
	@Override
	public void fire(Iterator<Document> documents) throws WorkflowException,
			MappingException, RepositoryException, RemoteException {
		while (documents.hasNext()) {
			this.publish(documents.next());
		}
	}
}

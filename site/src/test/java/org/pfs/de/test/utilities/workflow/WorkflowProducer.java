package org.pfs.de.test.utilities.workflow;

import javax.jcr.Node;
import javax.jcr.Session;

import org.hippoecm.repository.api.Workflow;

/**
 * Interface for class which produce workflows.
 * 
 * @author Martin Dreier <martin@martindreier.de>
 *
 * @param <T> Workflow type this producer produces.
 */
public interface WorkflowProducer<T extends Workflow> {
	
	public T createWorkflow(Node node, Session session);
}
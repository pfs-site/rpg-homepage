/**
 * 
 */
package org.pfs.de.test.utilities.workflow;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.hippoecm.repository.api.Document;
import org.hippoecm.repository.api.HippoWorkspace;
import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.api.WorkflowManager;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Utility class to mock {@link Workspace workspaces} and {@link Workflow workflows}.
 * 
 * These are currently not supported by the Hippo Utilities project.
 * 
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class WorkflowMockUtils {
	/**
	 * Create a workspace mock.
	 * @param session Hippo Session. Workspace will be attached to this session object.
	 * Must be a Mockito mock object.
	 * @throws RepositoryException 
	 */
	public static void mockWorkspace(final Session session) throws RepositoryException {
		HippoWorkspace workspace = Mockito.mock(HippoWorkspace.class);

		WorkflowManager workflowManager = Mockito.mock(WorkflowManager.class);
		Answer<Workflow> workflowAnswer = new Answer<Workflow>() {

			@Override
			public Workflow answer(InvocationOnMock invocation) throws Throwable {
				String workflowCategory = (String) invocation.getArguments()[0];
				Node node;
				if (invocation.getArguments()[1] instanceof Node) {
					node = (Node) invocation.getArguments()[1];
				} else if (invocation.getArguments()[1] instanceof Document) {
					node = session.getNodeByIdentifier(((Document)invocation.getArguments()[1]).getIdentity());
				} else {
					throw new ClassCastException(String.format("Second argument is unexpected class %s, expecting Node or Document", invocation.getArguments()[1].getClass().getName()));
				}
				return WorkflowProducerFactory.getProducer(workflowCategory).createWorkflow(node);
			}
			
		};
		Mockito.when(workflowManager.getWorkflow(Matchers.anyString(), Matchers.any(Document.class))).thenAnswer(workflowAnswer);
		Mockito.when(workflowManager.getWorkflow(Matchers.anyString(), Matchers.any(Node.class))).thenAnswer(workflowAnswer);
		Mockito.when(workflowManager.getSession()).thenReturn(session);
		
		Mockito.when(workspace.getSession()).thenReturn(session);
		Mockito.when(workspace.getWorkflowManager()).thenReturn(workflowManager);
		
		Mockito.when(session.getWorkspace()).thenReturn(workspace);
	}	
}

/**
 * 
 */
package org.pfs.de.test.utilities.workflow;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.standardworkflow.FolderWorkflow;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class FolderWorkflowProducer implements WorkflowProducer<FolderWorkflow> {

	/**
	 * @see org.pfs.de.test.utilities.workflow.WorkflowProducer#createWorkflow(javax.jcr.Node)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public FolderWorkflow createWorkflow(Node node, Session session) {

		ExceptionAnswer ex = new ExceptionAnswer();

		// Create workflow with exception for non-implemented methods
		FolderWorkflow workflow = Mockito.mock(FolderWorkflow.class, ex);

		// Create mocked methods
		try {
			// Add new document
			Answer<String> addAnswer = addAnswer(node, session);
			Mockito.doAnswer(addAnswer)
					.when(workflow)
					.add(Matchers.anyString(), Matchers.anyString(),
							Matchers.anyMap());
			Mockito.doAnswer(addAnswer)
					.when(workflow)
					.add(Matchers.anyString(), Matchers.anyString(),
							Matchers.anyString());
			Mockito.doReturn(Collections.<String, Serializable> emptyMap())
					.when(workflow).hints();
		} catch (RemoteException | WorkflowException | RepositoryException e) {
			throw new RuntimeException(e);
		}

		return workflow;
	}

	private static Answer<String> addAnswer(final Node node,
			final Session session) {
		return new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				// Read arguments
				String category = (String) invocation.getArguments()[0];
				String template = (String) invocation.getArguments()[1];
				String name;
				if (invocation.getArguments()[2] instanceof Map) {
					name = (String) ((Map<?, ?>) invocation.getArguments()[2])
							.get("name");
				} else {
					name = (String) invocation.getArguments()[2];
				}

				System.out.println(String.format(
						"Create node with category %s, template %s, name %s",
						category, template, name));

				Node newNode = node.addNode(name, template);
				//Node has been replaced with enhanced node, so
				// reference is now invalid => Re-read new node instance
				newNode = node.getNode(name);
				
				//Add default properties
				newNode.setProperty("hippostdpubwf:creationDate", new GregorianCalendar());
				
				return newNode.getPath();
			}
		};
	}

	/**
	 * Answer producing an exception when called. To be used to mark
	 * non-implemented methods. Throws a {@link NoSuchMethodException}
	 * with the passed arguments in the message.
	 * 
	 * @author Martin Dreier <martin@martindreier.de>
	 *
	 */
	private static final class ExceptionAnswer implements Answer<Object> {

		/**
		 * @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
		 */
		@Override
		public Object answer(InvocationOnMock invocation) throws Throwable {
			String methodName = invocation.getMethod().getName();
			List<Object> args = Arrays.asList(invocation.getArguments());
			throw new NoSuchMethodException(String.format(
					"Method not supported: %s (%s)", methodName, args));
		}

	}
}

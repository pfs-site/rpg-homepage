/**
 * 
 */
package org.pfs.de.test;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hippoecm.hst.configuration.hosting.Mount;
import org.hippoecm.hst.core.container.ContainerConstants;
import org.hippoecm.hst.core.linking.HstLink;
import org.hippoecm.hst.core.linking.HstLinkCreator;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.core.request.ResolvedMount;
import org.hippoecm.hst.jaxrs.services.AbstractResource;
import org.hippoecm.hst.mock.core.component.MockHstRequest;
import org.hippoecm.hst.mock.core.component.MockHstResponse;
import org.hippoecm.hst.mock.core.container.MockComponentManager;
import org.hippoecm.hst.mock.core.request.MockHstRequestContext;
import org.hippoecm.hst.site.HstServices;
import org.hippoecm.hst.site.container.SpringMetadataReaderClasspathResourceScanner;
import org.hippoecm.hst.util.ClasspathResourceScanner;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pfs.de.test.utilities.JcrMockUtils;
import org.pfs.de.test.utilities.workflow.WorkflowMockUtils;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.ServletContextResourceLoader;

/**
 * Base class for tests which require a running HST session. Call 
 * {@link #initializeRequest()} to prepare objects for the test.
 * 
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public abstract class BaseSessionTest {
	/**
	 * Mocked servlet request.
	 */
	protected MockHstRequest servletRequest = new MockHstRequest();
	/**
	 * Mocked servlet response.
	 */
	protected MockHstResponse servletResponse = new MockHstResponse();
	/**
	 * Mocked request context.
	 */
	protected MockHstRequestContext context = new MockHstRequestContext();
	
	/**
	 * Initialize the session. Loads test data into repository and fills mocked objects.
	 * 
	 * @throws RepositoryException
	 */
	protected void initializeRequest() throws RepositoryException {
		//Initialize servlet request
		servletRequest.setRequestContext(context);
		servletRequest.setContextPath("/");
		servletRequest.setAttribute(ContainerConstants.HST_REQUEST_CONTEXT,
				context);

		//Create servlet context
		MockServletContext servletContext = new MockServletContext();
		servletContext.addInitParameter(AbstractResource.BEANS_ANNOTATED_CLASSES_CONF_PARAM, "classpath*:org/pfs/de/beans/**/*.class,classpath*:org/onehippo/forge/feed/**/*.class");
		context.setServletContext(servletContext);
		context.setResolvedMount(createResolvedMount());
		context.setSession(createSession());
		context.setHstLinkCreator(createLinkCreator());
		
		//Build Sptring component manager
		MockComponentManager componentManager = new MockComponentManager();
		SpringMetadataReaderClasspathResourceScanner scanner = new SpringMetadataReaderClasspathResourceScanner();
		scanner.setResourceLoader(new ServletContextResourceLoader(servletContext));
		componentManager.addComponent(ClasspathResourceScanner.class.getName(), scanner);
        HstServices.setComponentManager(componentManager);
	}
	
	/**
	 * Create an instance of {@link HstLinkCreator} returning mocked
	 * values.
	 * 
	 * @return Link creator instance.
	 */
	private HstLinkCreator createLinkCreator() {
		Answer<Object> linkAnswer = new Answer<Object>() {
			
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Class<?> returnType = invocation.getMethod().getReturnType();
				if (returnType == HstLink.class) {
					MockHstLink link = new MockHstLink("http://www.example.com");
					return link;
				} else if (returnType == List.class) {
					List<HstLink> links = new ArrayList<HstLink>(5);
					for (int index = 1; index <= 5; index++) {
						MockHstLink link = new MockHstLink("http://www.example.com/" + index);
						links.add(link);
					}
					return links;
				} else {
					throw new IllegalArgumentException(String.format("Unhandled method: %s", invocation.getMethod().getName()));
				}
			}
		};
		HstLinkCreator linkCreator = Mockito.mock(HstLinkCreator.class, linkAnswer);
		return linkCreator;
	}

	/**
	 * Create mocks for the resolved mount.
	 * 
	 * @return Mock of the resolved mount;
	 */
	private ResolvedMount createResolvedMount() {
		Mount parent = Mockito.mock(Mount.class);
		Mount mount = Mockito.mock(Mount.class);
		Mockito.when(mount.getContentPath()).thenReturn("/content/documents/website");
		Mockito.when(mount.getParent()).thenReturn(parent);
		
		ResolvedMount resolvedMount = Mockito.mock(ResolvedMount.class);
		Mockito.when(resolvedMount.getMount()).thenReturn(mount);
		
		return resolvedMount;
	}
	
	/**
	 * Create a JCR session. Session object will also be enhanced with
	 * session data.
	 * 
	 * @return Test session.
	 * @throws RepositoryException
	 */
	private Session createSession() throws RepositoryException {
		//Create mock session with test data
		Session session = JcrMockUtils.mockDefaultJcrSession();
		
		//Hippo Utilities do not support workspace (yet)
		WorkflowMockUtils.mockWorkspace(session);
		
		return session;
	}
	
	/**
	 * Enhancement of the {@link org.hippoecm.hst.mock.core.linking.MockHstLink} class.
	 * 
	 * @author Martin Dreier <martin@martindreier.de>
	 *
	 */
	public class MockHstLink extends org.hippoecm.hst.mock.core.linking.MockHstLink {

		/**
		 * URL form of the link.
		 */
		private String urlForm;

		/**
		 * Create a new mock link.
		 * 
		 * @param urlForm The resolved URL form of this link.
		 */
		public MockHstLink(String urlForm) {
			super();
			this.urlForm = urlForm;
		}
		
		/**
		 * @see org.hippoecm.hst.mock.core.linking.MockHstLink#toUrlForm(org.hippoecm.hst.core.request.HstRequestContext, boolean)
		 */
		@Override
		public String toUrlForm(HstRequestContext requestContext, boolean fullyQualified) {
			return urlForm;
		}

	}
}

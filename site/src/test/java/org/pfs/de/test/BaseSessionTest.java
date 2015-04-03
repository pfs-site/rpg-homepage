/**
 * 
 */
package org.pfs.de.test;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hippoecm.hst.configuration.hosting.Mount;
import org.hippoecm.hst.core.container.ContainerConstants;
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
		
		//Build Sptring component manager
		MockComponentManager componentManager = new MockComponentManager();
		SpringMetadataReaderClasspathResourceScanner scanner = new SpringMetadataReaderClasspathResourceScanner();
		scanner.setResourceLoader(new ServletContextResourceLoader(servletContext));
		componentManager.addComponent(ClasspathResourceScanner.class.getName(), scanner);
        HstServices.setComponentManager(componentManager);
	}
	
	/**
	 * Create mocks for the resolved mount.
	 * 
	 * @return Mock of the resolved mount;
	 */
	private ResolvedMount createResolvedMount() {
		Mount mount = Mockito.mock(Mount.class);
		Mockito.when(mount.getContentPath()).thenReturn("/content");
		
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
}

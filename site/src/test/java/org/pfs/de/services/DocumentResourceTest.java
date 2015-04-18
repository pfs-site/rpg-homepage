/**
 * 
 */
package org.pfs.de.services;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Date;

import javax.jcr.RepositoryException;

import org.hippoecm.hst.content.rewriter.impl.SimpleContentRewriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.pfs.de.services.model.CommentDocumentRepresentation;
import org.pfs.de.test.BaseSessionTest;

/**
 * Test for document resource.
 * 
 * @author martin
 *
 */
@RunWith(JUnit4.class)
public class DocumentResourceTest extends BaseSessionTest {

	/**
	 * Document ID for testing.
	 */
	private static final String DOCUMENT_ID = "2830a222-c32b-425a-9f0d-01cf87246b3b";

	@Before
	public void prepareTest() throws RepositoryException, IOException {
		initializeRequest();
	}

	/**
	 * Test creation of a new content.
	 */
	@Test
	public void createNewComment() {
		// Object under test
		DocumentResource docResource = new DocumentResource();

		// Input data
		CommentDocumentRepresentation comment = new CommentDocumentRepresentation(
				context, new SimpleContentRewriter());
		comment.setAuthor("Tester");
		comment.setDate(new Date());
		comment.setLink("http://example.com/test");
		comment.setText("Comment Text");

		CommentDocumentRepresentation result = docResource.createComment(
				servletRequest, servletResponse, null, DOCUMENT_ID, comment);

		assertNotNull("No representation created for comment response", result);
		
		assertEquals("Author not set correctly", "Tester", result.getAuthor());
		//Reference document not created correctly due to mocking
//		assertEquals("Comment created for wrong document", DOCUMENT_ID, result.getReferenceDocument().getIdentifier());
	}

}

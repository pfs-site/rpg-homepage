/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pfs.de.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.hippoecm.hst.content.annotations.Persistable;
import org.hippoecm.hst.content.beans.ObjectBeanManagerException;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.core.linking.HstLink;
import org.pfs.de.akismet.AkismetCommentData;
import org.pfs.de.beans.BaseDocument;
import org.pfs.de.beans.BlogDocument;
import org.pfs.de.beans.CommentDocument;
import org.pfs.de.services.model.CommentDocumentRepresentation;

/**
 * REST-based web service for documents.
 * 
 * @author Martin Dreier
 */

@Path("/documents/")
public class DocumentResource extends BaseResource implements BaseResource.AkismetConversionCallback<CommentDocument>{
    
    /**
     * Get a single document. As this service is not (yet) provided, this method
     * redirects to the comments of the selected document.
     * @param servletRequest Request object.
     * @param servletResponse Response object.
     * @param documentId The UUID of the document.
     * @throws IOException 
     */
    @GET
    @Path("/{documentId}")
    public void getDocument(@Context HttpServletRequest servletRequest, 
    						@Context HttpServletResponse servletResponse,
							@PathParam("documentId") String documentId) 
									throws ServletException {
        try {
            servletResponse.sendRedirect(String.format("comments", documentId));
        } catch (IOException ex) {
            Logger.getLogger(DocumentResource.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServletException(ex);
        }
    }
    
    /**
     * Get comments referring to a document. Returns a 404 error if the
     * document identified by <code>documentId</code> does not exist.
     * @param servletRequest Request object.
     * @param servletResponse Response object.
     * @param documentId <em>Path Parameter.</em> UUID of the document to which
     * the comments should refer.
     * @return A list of comments referring to the document identified by the
     * <code>documentId</code>.
     * @throws ServletException 
     */
    @GET
    @Path("/{documentId}/comments")
    public List<CommentDocumentRepresentation> getComments(@Context HttpServletRequest servletRequest, 
												            @Context HttpServletResponse servletResponse,
												            @PathParam("documentId") String documentId) 
												            		throws ServletException {
        try {
            //Assert that document exists
            BaseDocument baseDoc = getDocumentById(servletRequest, BaseDocument.class, documentId);
            if (baseDoc == null) {
                servletResponse.sendError(404, "Requested document does not exist");
                return Collections.emptyList();
            }
            //Get comments
            List<CommentDocument> commentDocuments = getCommentsByRef(servletRequest, documentId, 0);
            List<CommentDocumentRepresentation> comments = new ArrayList<CommentDocumentRepresentation>(commentDocuments.size());
            for (CommentDocument doc: commentDocuments) {
                comments.add(new CommentDocumentRepresentation(getRequestContext(servletRequest), getContentRewriter()).represent(doc));
            }
            return comments;
            
        } catch (RepositoryException ex) {
            Logger.getLogger(DocumentResource.class.getName()).log(Level.SEVERE, "Error reading requested document", ex);
            throw new ServletException(ex);
        } catch (ObjectBeanManagerException ex) {
            Logger.getLogger(DocumentResource.class.getName()).log(Level.SEVERE, "Error retrieving bean manager", ex);
            throw new ServletException(ex);
        } catch (IOException ex) {
            Logger.getLogger(DocumentResource.class.getName()).log(Level.SEVERE, "Error sending response data", ex);
            throw new ServletException(ex);
        } catch (QueryException ex) {
            Logger.getLogger(DocumentResource.class.getName()).log(Level.SEVERE, "Error searchng for comments", ex);
            throw new ServletException(ex);
        }
        
    }
    
    /**
     * Get a single comment. Returns a 404 error if the base or comment documents
     * do not exist, or if the comment does not refer to the base document.
     * @param servletRequest Request object.
     * @param servletResponse Response object.
     * @param documentId The UUID of the base document.
     * @param commentId The UUID of the comment document.
     * @return The identified document, or <code>null</code> if no document can
     * be found.
     * @throws ServletException 
     */
    @GET
    @Path("/{documentId}/comments/{commentId}")
    public CommentDocumentRepresentation getComment(@Context HttpServletRequest servletRequest, 
										            @Context HttpServletResponse servletResponse,
										            @PathParam("documentId") String documentId,
										            @PathParam("commentId") String commentId) 
										            		throws ServletException {
        try {
            //Assert that document exists
            BaseDocument baseDoc = getDocumentById(servletRequest, BaseDocument.class, documentId);
            if (baseDoc == null) {
                servletResponse.sendError(404, "Requested document does not exist");
                return null;
            }
            
            //Get comment
            CommentDocument commentDoc = getDocumentById(servletRequest, CommentDocument.class, commentId);
            if (commentDoc == null) {
                servletResponse.sendError(404, "Requested comment does not exist");
                return null;
            }
            //Check that comment actually refers to the base document, but only if
            // reference is set
            HippoDocument refDoc = commentDoc.getReferencedDocument();
            if (refDoc != null && !refDoc.equals(baseDoc)){
                servletResponse.sendError(404, "Requested comment does not exist");
                return null;
            }
            return new CommentDocumentRepresentation(getRequestContext(servletRequest), getContentRewriter()).represent(commentDoc);
        } catch (RepositoryException ex) {
            Logger.getLogger(DocumentResource.class.getName()).log(Level.SEVERE, "Error reading document", ex);
            throw new ServletException(ex);
        } catch (ObjectBeanManagerException ex) {
            Logger.getLogger(DocumentResource.class.getName()).log(Level.SEVERE, "Error getting bean manager", ex);
            throw new ServletException(ex);
        } catch (IOException ex) {
            Logger.getLogger(DocumentResource.class.getName()).log(Level.SEVERE, "Error writing response", ex);
            throw new ServletException(ex);
        }
    }
    
    /**
     * Create a new comment document.
     * @param servletRequest The request object.
     * @param servletResponse Response object.
     * @param uriInfo URI info.
     * @param documentReference The UUID of the document for which the comment is created.
     * @param commentRepresentation The comment data.
     * @return The complete comment, or <code>null</code> if the comment could not be created.
     */
    @Persistable
    @POST
    @Path("/{documentId}/comments")
    public CommentDocumentRepresentation createComment(@Context HttpServletRequest servletRequest, 
    													@Context HttpServletResponse servletResponse, 
    													@Context UriInfo uriInfo,
														@PathParam("documentId") String documentId, 
														CommentDocumentRepresentation commentRepresentation) {

            
        try {
            //Check that all parameters are available
            if (documentId == null || documentId.length() == 0) {
                servletResponse.sendError(400, "Invalid document ID");
                return null;
            }
            if (commentRepresentation == null) {
                servletResponse.sendError(400, "Comment data missing");
                return null;
            }
            if (isEmpty(commentRepresentation.getAuthor()) ||
                    isEmpty(commentRepresentation.getText())) {
                servletResponse.sendError(400, "Comment incomplete");
                return null;
            }
            //Try to find the requested document
            BaseDocument document = getDocumentById(servletRequest, BaseDocument.class, documentId);
            
            if (document == null) {
                servletResponse.sendError(404, "Requested document not found");
                return null;
            }
            boolean commentsAllowed = false;
            //Check that comments are allowed
            if (document instanceof BlogDocument && ((BlogDocument)document).getCommentsAllowed()) {
                commentsAllowed = true;
            }
            //TODO: Check for other document types

            if (!commentsAllowed) {
                servletResponse.sendError(403, "Comments are disabled on this document");
                return null;
            }

            String name;
            if (! isEmpty(commentRepresentation.getName())) {
                name = commentRepresentation.getName();
            } else {
                name = String.format("%s-comment-at-%2$tY%2$tm%2$td-%2$tH%2$tM%2$tS-%2$tL", "test", new Date());
            }
            
            commentRepresentation.setReferenceDocument(document);
            
            CommentDocument newComment = createNewDocument(servletRequest, "/comments", "website:commentdocument", name, commentRepresentation, this);
            if (newComment == null) {
            	servletResponse.sendError(400, "Comment not accepted");
            	return null;
            } else {
            	return new CommentDocumentRepresentation(getRequestContext(servletRequest), getContentRewriter()).represent(newComment);
            }

        } catch (Exception e) {
            throw new WebApplicationException(e);
        }

    }
    
    /**
     * Read data from comment and store in Aksimet data structure.
     * @see AkismetConversionCallback#convert(org.pfs.de.beans.BaseDocument)
     */
	@Override
	public AkismetCommentData convert(HttpServletRequest request, CommentDocument comment) {
		AkismetCommentData data = new AkismetCommentData();
		data.setAuthorName(comment.getAuthor());
		data.setCommentDate(comment.getDate());
		data.setIdentifier(comment.getIdentifier());
		data.setAuthorUrl(comment.getLink());
		data.setCommentContent(comment.getText());
		data.setBlogUrl(getRequestContext(request).getResolvedMount().getResolvedVirtualHost().getVirtualHost().getHomePage());
		if (comment.getReferencedDocument() != null) {
			HippoDocument refDoc = comment.getReferencedDocument();
			if (refDoc instanceof BlogDocument) {
				data.setDocumentDate(((BlogDocument) refDoc).getDate());
			}
			HstLink refLink = getRequestContext(request).getHstLinkCreator().create(refDoc, getRequestContext(request));
			if (refLink != null) {
				data.setPermalink(refLink.toUrlForm(getRequestContext(request), true));
			}
		}
		return data;
	}
}

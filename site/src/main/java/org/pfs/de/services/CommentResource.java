/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pfs.de.services;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.pfs.de.beans.CommentDocument;
import org.pfs.de.services.model.CommentDocumentRepresentation;

/**
 * Web service provider for comments.
 *
 * @author Martin Dreier
 */
@Path("/comments/")
public class CommentResource extends BaseResource {

    /**
     * Get a single comment.
     *
     * @param servletRequest Request object.
     * @param servletResponse Response object.
     * @param uriInfo URI information.
     * @param commentId UUID of a comment document.
     * @return The comment identified by the UUID in the
     * <code>commentId</code> parameter.
     */
    @GET
    @Path("/{commentId}")
    public CommentDocumentRepresentation getComment(@Context HttpServletRequest servletRequest, @Context HttpServletResponse servletResponse,
            @Context UriInfo uriInfo, @PathParam("commentId") String commentId) {

        try {
            CommentDocument comment = getDocumentById(servletRequest, CommentDocument.class, commentId);
            if (comment == null) {
                servletResponse.sendError(404, "Requested comment not found");
                return null;
            }
            return new CommentDocumentRepresentation(getRequestContext(servletRequest), getContentRewriter()).represent(comment);
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
    
}

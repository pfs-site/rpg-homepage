/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pfs.de.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import org.hippoecm.hst.content.annotations.Persistable;
import org.hippoecm.hst.content.beans.ObjectBeanManagerException;
import org.hippoecm.hst.content.beans.manager.ObjectBeanManager;
import org.hippoecm.hst.content.beans.manager.ObjectBeanManagerImpl;
import org.hippoecm.hst.content.beans.manager.workflow.WorkflowPersistenceManager;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryManager;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.hippoecm.hst.content.beans.query.filter.Filter;
import org.hippoecm.hst.content.beans.standard.HippoBeanIterator;
import org.hippoecm.hst.content.beans.standard.HippoFolderBean;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.jaxrs.services.AbstractResource;
import org.hippoecm.hst.util.PathUtils;
import org.pfs.de.beans.BaseDocument;
import org.pfs.de.beans.CommentDocument;
import org.pfs.de.services.model.BaseDocumentRepresentation;

/**
 * Base class for REST-based services. Provides utility methods to read
 * documents and comments from the repository.
 * 
 * @author Martin Dreier
 */
public abstract class BaseResource extends AbstractResource {

    /**
     * Retrieve a document by its ID.
     *
     * @param servletRequest Request object.
     * @param documentClass The class of the document to be retrieved.
     * @param documentId The UUID of the document.
     * @return The document, or <code>null</code> if no document with this UUID
     * is found or if the document identified by the UUID is not an instance of
     * the <code>documentClass</code>, or if the <code>documentId</code>      
     * is <code>null</code>.
     * @throws ObjectBeanManagerException An error occurred while retrieving the
     * {@link ObjectBeanManager}.
     * @throws RepositoryException An error occurred retrieving the document
     * from the repository.
     */
    protected <T> T getDocumentById(HttpServletRequest servletRequest, Class<T> documentClass, String documentId) throws RepositoryException, ObjectBeanManagerException {
        if (documentId == null) {
            return null;
        }
        
        HstRequestContext requestContext = getRequestContext(servletRequest);
        Object objectRef = getObjectBeanManager(requestContext).getObjectByUuid(documentId.toLowerCase());
        if (objectRef != null && documentClass.isAssignableFrom(objectRef.getClass())) {
            return (T) objectRef;
        }
        //Nothing found, or wrong type
        return null;
    }

    /**
     * Get comments by reference to a document.
     * @param servletRequest The request object.
     * @param documentId The UUID of the document to which the comments should
     * be referring.
     * @param limit The maximum number of comments to be returned. Set to
     * <code>0</code> to switch off the limit.
     * @return A list of comments which refer to the document which is
     * identified by the UUID. This list may be empty, and it may be 
     * non-mutable.
     * @throws RepositoryException An error occurred while accessing the 
     * repository.
     * @throws QueryException An error occurred while executing the search.
     */
    protected List<CommentDocument> getCommentsByRef(HttpServletRequest servletRequest, String documentId, int limit) throws RepositoryException, QueryException {
        if (documentId == null) {
            return null;
        }
        
        //Get the references to the context and the query manager
        HstRequestContext requestContext = getRequestContext(servletRequest);
        HstQueryManager hstQueryManager = getHstQueryManager(requestContext.getSession(), requestContext);

        //Determine search base
        String mountContentPath = requestContext.getResolvedMount().getMount().getContentPath();
        Node mountContentNode = requestContext.getSession().getRootNode().getNode(PathUtils.normalizePath(mountContentPath));

        //Create and execute query
        HstQuery hstQuery = hstQueryManager.createQuery(mountContentNode, CommentDocument.class);
        Filter filter = hstQuery.createFilter();
        filter.addEqualTo("website:reference/@hippo:docbase", documentId.toLowerCase());
        hstQuery.setFilter(filter);
        if (limit > 0) {
            hstQuery.setLimit(limit);
        }

        HstQueryResult result = hstQuery.execute();
        
        if (result.getSize() == 0) {
            return Collections.emptyList();
        }
        
        //Read results
        List<CommentDocument> comments = new ArrayList<CommentDocument>(result.getSize());
        HippoBeanIterator iterator = result.getHippoBeans();

        while (iterator.hasNext()) {
            //Get all incoming comment links
            CommentDocument comment = (CommentDocument) iterator.nextHippoBean();

            if (comment != null) {
                comments.add(comment);
            }
        }

        return comments;
    }

    /**
     * Get the object bean manager.
     *
     * @param requestContext The request context.
     * @return The Object bean manager from the provided context.
     * @throws RepositoryException
     */
    protected ObjectBeanManager getObjectBeanManager(HstRequestContext requestContext) throws RepositoryException {
        //TODO: Replace with call to requestContext.getObjectBeanManager() after
        // update to newer Hippo release
        return new ObjectBeanManagerImpl(requestContext.getSession(), getObjectConverter(requestContext));
    }

    /**
     * Check if a text is empty.
     *
     * @param text The text to check. May be <code>null</code>.
     * @return <code>true</code> if the text is <code> null</code> or empty,
     * <code>false</code> otherwise.
     */
    protected boolean isEmpty(String text) {
        if (text == null) {
            return true;
        }

        if (text.trim().length() == 0) {
            return true;
        }
        return false;
    }
    
    /**
     * Create a new document. This method <em>must</em> be called from a
     * method annotated with {@link Persistable @Persistable} to ensure that
     * a writable session is used.
     * @param <T> The type of the document which is created. This type must
     * be annotated with {@link Node @Node(jcrType="type")}, where type is the
     * same type as set in the parameter <code>documentType</code>.
     * @param request Request object.
     * @param path Path in the repository where the new document is stored.
     * @param documentType The type of the document being created. This document
     * type must be represented by the bean class <code>T</code>.
     * @param name The name of the new document.
     * @param representation The document representation holding the data.
     * @return The new document.
     * @throws ObjectBeanManagerException Error getting the bean manager.
     * @throws RepositoryException Error storing the data.
     */
    protected <T extends BaseDocument> T createNewDocument(HttpServletRequest request, String path, String documentType, String name, BaseDocumentRepresentation representation) throws ObjectBeanManagerException, RepositoryException {
        HstRequestContext requestContext = getRequestContext(request);
        WorkflowPersistenceManager persistanceManager = (WorkflowPersistenceManager) getPersistenceManager(requestContext);
        
        
        //Get bean instance
        HippoFolderBean contentBaseFolder = getMountContentBaseBean(requestContext);
        String commentFolderPath = contentBaseFolder.getPath() + path;
        String beanPath = persistanceManager.createAndReturn(commentFolderPath, documentType, name, true);
        T document = (T) persistanceManager.getObject(beanPath);

        //Load data
        document.update(representation);

        //Save bean in repository
        persistanceManager.update(document);
        persistanceManager.save();

        //Read back complete bean instance for return
        return (T) persistanceManager.getObject(beanPath);
    }
}

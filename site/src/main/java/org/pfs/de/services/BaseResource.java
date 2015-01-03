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
import org.pfs.de.akismet.AkismetCheckResult;
import org.pfs.de.akismet.AkismetClient;
import org.pfs.de.akismet.AkismetCommentData;
import org.pfs.de.akismet.AkismetConfiguration;
import org.pfs.de.akismet.AkismetException;
import org.pfs.de.beans.BaseDocument;
import org.pfs.de.beans.CommentDocument;
import org.pfs.de.events.AutomaticPublicationSubscriber;
import org.pfs.de.events.AutomaticPublicationSubscriber.PublishAction;
import org.pfs.de.services.model.BaseDocumentRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.HttpHeaders;

/**
 * Base class for REST-based services. Provides utility methods to read
 * documents and comments from the repository.
 * 
 * @author Martin Dreier
 */
public abstract class BaseResource extends AbstractResource {

	/**
	 * Read data required for an Akismet comment check from a document.
	 * @author Martin Dreier <martin@martindreier.de>
	 *
	 * @param <T> The document type.
	 */
	protected interface AkismetConversionCallback<T extends BaseDocument> {
		/**
		 * Convert document into Akismet data.
		 * @param servletRequest The current request.
		 * @param document The document.
		 * @param representation Document representation. Use instead of document bean because bean does not have
		 * reference to document before save.
		 * @return Akismet data.
		 */
		public AkismetCommentData convert(HttpServletRequest servletRequest, T document, BaseDocumentRepresentation representation);
	}
	
	/**
	 * Logging instance.
	 */
	private static Logger log = LoggerFactory.getLogger(BaseResource.class);
	
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
    @SuppressWarnings("unchecked")
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
        @SuppressWarnings("unchecked")
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
    protected <T extends BaseDocument> T createNewDocument(HttpServletRequest request, 
    														String path, 
    														String documentType, 
    														String name, 
    														BaseDocumentRepresentation representation) 
    																throws ObjectBeanManagerException, 
    																	RepositoryException {
    	return createNewDocument(request, path, documentType, name, representation, null);
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
     * @param akismetCallback Callback instance which can read data for an Akismet spam
     * check from the new document.
     * @return The new document.
     * @throws ObjectBeanManagerException Error getting the bean manager.
     * @throws RepositoryException Error storing the data.
     */
    @SuppressWarnings("unchecked")
	protected <T extends BaseDocument> T createNewDocument(HttpServletRequest request, 
    														String path, 
    														String documentType, 
    														String name, 
    														BaseDocumentRepresentation representation,
    														AkismetConversionCallback<T> akismetCallback) 
    																throws ObjectBeanManagerException, 
    																	RepositoryException {
    	
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
        
        //Check for spam
        if (akismetCallback != null) {
        	boolean continueProcessing = checkForSpam(request, document, akismetCallback.convert(request, document, representation));
        	if (!continueProcessing) {
        		return null;
        	}
        }
        
        persistanceManager.save();

        //Read back complete bean instance for return
        return (T) persistanceManager.getObject(beanPath);
    }

    /**
     * Check a comment for spam. The document will be marked with the correct action. If an error occurred,
     * it will be marked as {@link PublishAction#IGNORE ignore}.
     * @param request HTTP request.
     * @param document The checked document.
     * @param commentData Data of the comment.
     * @return Indicator if processing should be continued. If <code>false</code>, comment was rejected
     * and should not be saved. If <code>true</code>, comment can be saved. 
     * @throws RepositoryException
     */
    protected boolean checkForSpam(HttpServletRequest request, BaseDocument document, AkismetCommentData commentData) throws RepositoryException {
    	//Read configuration from repository
    	AkismetConfiguration configuration = AkismetConfiguration.readConfiguration(getRequestContext(request).getSession(), document.getNode());
    	if (!configuration.isComplete()) {
    		//Incomplete configuration
    		log.warn("Akismet configuration is incomplete for document {}", commentData.getIdentifier());
    		return true;
    	}
    	PublishAction hamAction = PublishAction.getAction(configuration.hamAction);
    	if (hamAction == null) {
    		log.error("Invalid Akismet ham actions: {} ", configuration.hamAction);
    		return true;
    	}
    	
    	//Complete comment data
    	commentData.setIdentifier(document.getIdentifier());
    	commentData.setUserIp(request.getRemoteAddr());
    	commentData.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
    	commentData.setReferrer(request.getHeader(HttpHeaders.REFERER));
    	
    	//Create client and check key
    	try {
    		PublishAction action;
			AkismetClient client = new AkismetClient(configuration.apiKey);
			if (!client.checkApiKey()) {
				log.error("Akismet API key is incorrect");
				action = PublishAction.IGNORE;
			} else {
				//Check comment
				AkismetCheckResult result = client.checkComment(commentData);
				if (result.isError()) {
					log.error("Akismet spam check failed for comment {}", commentData.getIdentifier());
					if (log.isDebugEnabled() && result.getAdditionalInformation().containsKey(AkismetCheckResult.INFO_DEBUG)) {
						//Write additional debug information
						log.debug("Error from Akismet server {}: {}", 
								result.getAdditionalInformation().get(AkismetCheckResult.INFO_SERVER),
								result.getAdditionalInformation().get(AkismetCheckResult.INFO_DEBUG));
					}
					return true;
				}
				
				//Process result
				switch (result.getResult()) {
				case HAM:
					action = hamAction;
					break;
				case SPAM:
				{
					if (configuration.spamAction == null) {
						//Default to ignore if not configured
						action = PublishAction.IGNORE;
					} else if (configuration.spamAction.equals(AkismetConfiguration.PROP_VALUE_SPAM_ACTION_REJECT)) {
						//Reject spam comment
						return false;
					} else {
						PublishAction spamAction = PublishAction.getAction(configuration.spamAction);
						if (spamAction == null) {
							log.error("Invalid Akismet spam action: {} ; defaulting to ignore", configuration.hamAction);
							action =  PublishAction.IGNORE;
						} else {
							action = spamAction;
						}
					}
					break;
				}
				case INVALID:
					//We should never reach this block, because errors are handled before (in block if (result.isError())...)
					// However, handle this situation gracefully in case of future changes influencing this behavior
					log.error("Akismet spam check failed for comment {}", commentData.getIdentifier());
					action = PublishAction.IGNORE;
				default:
					log.error("Akismet spam check returned unknown result type {}", result.getResult());
					action = PublishAction.IGNORE;
				}
			}
			//Set desired action on the document handle (parent node of current document)
			AutomaticPublicationSubscriber.setAutoPublishAction(getRequestContext(request).getSession().getNodeByIdentifier(document.getCanonicalHandleUUID()), action);
			
			return true;
			
		} catch (AkismetException e) {
			log.error("Akismet check failed", e);
			AutomaticPublicationSubscriber.setAutoPublishAction(document.getNode(), PublishAction.IGNORE);
			return true;
		}
    }
}

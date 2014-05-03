/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pfs.de.services.model;

import javax.jcr.RepositoryException;
import javax.xml.bind.annotation.XmlRootElement;
import org.hippoecm.hst.content.rewriter.ContentRewriter;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.jaxrs.model.content.HippoDocumentRepresentation;
import org.pfs.de.beans.BaseDocument;

/**
 * XML model for the base document.
 * 
 * @author pfs-programmierer
 */
@XmlRootElement(name = "basedocument")
public class BaseDocumentRepresentation extends HippoDocumentRepresentation {
    /**
     * The request context.
     */
    protected HstRequestContext requestContext;
    /**
     * The content rewriter.
     */
    protected ContentRewriter<String> contentRewriter;

    public BaseDocumentRepresentation() {
        
    }
    
    /**
     * Create a new representation of a base document.
     * @param requestContext The request context.
     * @param contentRewriter  The content rewriter.
     */
    public BaseDocumentRepresentation(HstRequestContext requestContext, ContentRewriter<String> contentRewriter){
 	this.requestContext = requestContext;
        this.contentRewriter = contentRewriter;
    }
    
    /**
     * Update this representation with the contents of a document.
     * This method updates and modifies this instance of the representation
     * and returns itself.
     * @param bean The base document bean which holds the data.
     * @return This instance of the representation.
     * @throws RepositoryException 
     */
    public BaseDocumentRepresentation represent(BaseDocument bean) throws RepositoryException {
        super.represent(bean);

        return this;
    }
}
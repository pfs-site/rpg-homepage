/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pfs.de.services.model;

import java.util.Date;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.bind.annotation.XmlRootElement;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.rewriter.ContentRewriter;
import org.hippoecm.hst.core.container.ContainerConstants;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.pfs.de.beans.CommentDocument;

/**
 *
 * @author pfs-programmierer
 */
@XmlRootElement(name = "comment")
public class CommentDocumentRepresentation extends BaseDocumentRepresentation {
    
    private String author;
    private Date date;
    private String text;
    private String link;
    private String referencedDocument;
    
    public CommentDocumentRepresentation() {
        
    }
    
    public CommentDocumentRepresentation(HstRequestContext requestContext, ContentRewriter<String> contentRewriter){
 	super(requestContext, contentRewriter);
    }
    
    /**
     * Create a representation for a comment document.
     * @param document The comment document.
     * @return A representation of the comment document.
     * @throws RepositoryException
     */
    public CommentDocumentRepresentation represent(CommentDocument document) throws RepositoryException {
        super.represent(document);
        
        //Read fields from comment document
        setAuthor(document.getAuthor());
        setDate(document.getDate());
        setLink(document.getLink());
        setText(document.getText());
        
        return this;
    }

    /**
     * Build a link to another bean.
     * @param reference The referenced bean. May be <code>null</code>.
     * @return A link to the referenced bean, or <code> null</code> if the
     * parameter is null or not a node.
     */
    protected String buildLink(HippoBean reference) {
        if (reference == null) {
            return null;
        }
        return buildLink(reference.getNode());
    }
    
    /**
     * Build a link to a node in the JCR.
     * @param reference The rtarget node. May be <code>null</code>.
     * @return A link to the referenced bean, or <code> null</code> if the
     * target node is <code>null</code>.
     */
    protected String buildLink(Node targetNode) {
        if (targetNode == null) {
            return null;
        }
        return requestContext.getHstLinkCreator()
                .create(targetNode, requestContext, ContainerConstants.MOUNT_ALIAS_SITE)
                .toUrlForm(requestContext, false);
    }
    
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}

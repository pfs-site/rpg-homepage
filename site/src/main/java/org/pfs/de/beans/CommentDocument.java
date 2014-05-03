package org.pfs.de.beans;

import java.util.Date;
import java.util.GregorianCalendar;
import javax.jcr.RepositoryException;
import org.hippoecm.hst.content.beans.ContentNodeBindingException;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.pfs.de.services.model.BaseDocumentRepresentation;
import org.pfs.de.services.model.CommentDocumentRepresentation;

/**
 * Bean representation of a comment document.
 * @author pfs-programmierer
 */
@Node(jcrType="website:commentdocument")
public class CommentDocument extends BaseDocument{
    protected static final String FIELD_AUTHOR = "website:author";
    protected static final String FIELD_LINK = "website:authorlink";
    protected static final String FIELD_TEXT = "website:commenttext";
    protected static final String FIELD_REFERENCE = "website:reference";

    /**
     * Get the author. Name of the author as given in the comment form.
     * @return The author name.
     */
    public String getAuthor() {
        return getProperty(FIELD_AUTHOR);
    }
    
    /**
     * Get the link URL. Link given by the author.
     * @return The author link.
     */
    public String getLink() {
        return getProperty(FIELD_LINK);
    }
    
    /**
     * Get the comment text.
     * @return The text content of the comment.
     */
    public String getText() {
        return getProperty(FIELD_TEXT);
    }
    
    /**
     * Get the referenced document.
     * @return The document that the comment was written in reference to.
     */
    public HippoDocument getReferencedDocument() {
        return getProperty(FIELD_REFERENCE);
    }
    
    /**
     * Get the date of the comment. This is the creation date of
     * the comment.
     * @return The publication date of the blog entry.
     */
    public Date getDate() {
        GregorianCalendar cal = getProperty("hippostdpubwf:creationDate");
        return cal.getTime();
    }
    
    /**
     * Update the data in this bean from a representation. Modifies only
     * fields managed in this subclass, but not in the superclass:
     * <ul>
     *   <li>Author</li>
     *   <li>Link</li>
     *   <li>Text</li>
     * </ul>
     * 
     * The referenced document and all other fields must be set separately.
     * 
     * @param representation The document representation.
     * @see #setReferencedDocument(org.hippoecm.hst.content.beans.standard.HippoDocument) 
     */
    @Override
    public void update(BaseDocumentRepresentation representation) throws RepositoryException {
        if (representation instanceof CommentDocumentRepresentation) {
            CommentDocumentRepresentation commentRepresentation = (CommentDocumentRepresentation) representation;
            this.getNode().setProperty(FIELD_AUTHOR, commentRepresentation.getAuthor());
            this.getNode().setProperty(FIELD_LINK, commentRepresentation.getLink());
            this.getNode().setProperty(FIELD_TEXT, commentRepresentation.getText());
        }
    }
    
    /**
     * Set the referenced document.
     * @param document The document that this comment was written for.
     * @see #update(org.pfs.de.services.model.CommentDocumentRepresentation) 
     */
    public void setReferencedDocument(HippoDocument document) throws RepositoryException, ContentNodeBindingException {
        addMirrorNode(this.getNode(), FIELD_REFERENCE, document.getCanonicalHandleUUID());
    }
}

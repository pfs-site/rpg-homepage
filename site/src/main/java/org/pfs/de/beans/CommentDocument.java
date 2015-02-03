package org.pfs.de.beans;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.RepositoryException;

import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.content.beans.ContentNodeBindingException;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.content.beans.standard.HippoMirror;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.onehippo.forge.feed.api.FeedType;
import org.onehippo.forge.feed.api.annot.SyndicationElement;
import org.pfs.de.services.model.BaseDocumentRepresentation;
import org.pfs.de.services.model.CommentDocumentRepresentation;

import com.sun.syndication.feed.rss.Description;
import com.sun.syndication.feed.rss.Guid;

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
    @SyndicationElement(type = FeedType.RSS, name = "author")
    public String getAuthor() {
        return getProperty(FIELD_AUTHOR);
    }
    
    /**
     * Get the author. Name of the author as given in the comment form.
     * @return The author name.
     */
    @SyndicationElement(type = FeedType.RSS, name = "title")
    public String getTitle() {
        return "Kommentar von " + getProperty(FIELD_AUTHOR) + " am " + getDate();
    }
    
    /**
     * Get the link URL. Link given by the author.
     * @return The author link.
     */
    public String getLink() {
        return getProperty(FIELD_LINK);
    }

    /**
     * Get the URL pointing to the comment's article. 
     * @return The link to the comment's article.
     */
    @SyndicationElement(type = FeedType.RSS, name = "link")
    public String getSyndicationLink() {
    	final HstRequestContext hstRequestContext = RequestContextProvider.get();
        return hstRequestContext.getHstLinkCreator().create(this.getReferencedDocument(), hstRequestContext).toUrlForm(hstRequestContext, true);
    }
    
    /**
     * Get the comment text.
     * @return The text content of the comment.
     */
    public String getText() {
        return getProperty(FIELD_TEXT);
    }
    
    @SyndicationElement(type = FeedType.RSS, name = "description")
    public Description getDescription() {
    	Description ret = new Description();
    	
    	ret.setValue(this.getText());
    	ret.setType(null);
    	
    	return ret;
    }
    
    /**
     * Get the referenced document.
     * @return The document that the comment was written in reference to.
     */
    public HippoDocument getReferencedDocument() {
    	HippoDocument ret = null;
    	List<Object> docs = getChildBeansByName(FIELD_REFERENCE);
    	HippoMirror mirror = null;
    	HippoBean bean = null;
    	if((docs !=  null) && (docs.size() > 0) && (docs.get(0) instanceof HippoMirror)) {
    		mirror = (HippoMirror) docs.get(0);
    		if(mirror != null) {
        		bean = mirror.getReferencedBean();
    		}
    	}
    	if((bean != null) && (bean.isHippoDocumentBean())) {
    		ret = (HippoDocument)bean;
    	}
        return ret;
    }
    
    /**
     * Get the date of the comment. This is the creation date of
     * the comment.
     * @return The publication date of the blog entry.
     */
    @SyndicationElement(type = FeedType.RSS, name = "pubDate")
    public Date getDate() {
        GregorianCalendar cal = getProperty("hippostdpubwf:creationDate");
        return cal.getTime();
    }
    
    @SyndicationElement(type = FeedType.RSS, name = "guid")
    public Guid getGuid() {
    	Guid ret = new Guid();
    	
    	ret.setPermaLink(true);
    	ret.setValue(this.getSyndicationLink());
    	
    	return ret;
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
            try {
				this.setReferencedDocument(commentRepresentation.getReferenceDocument());
			} catch (ContentNodeBindingException e) {
				String msg = "Unable to set reference document for comment \"" + commentRepresentation.getName() + "\".";
				RepositoryException repoEx = new RepositoryException(msg, e);
				throw repoEx;
			}
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

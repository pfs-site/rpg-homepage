package org.pfs.de.beans;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.jcr.RepositoryException;

import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSetBean;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.jsoup.Jsoup;
import org.onehippo.forge.feed.api.FeedType;
import org.onehippo.forge.feed.api.annot.SyndicationElement;
import org.pfs.de.services.model.BaseDocumentRepresentation;

import com.sun.syndication.feed.rss.Description;
import com.sun.syndication.feed.rss.Guid;

@Node(jcrType="website:blogdocument")
public class BlogDocument extends BaseDocument{
    /**
     * Maximum length of the summary. Must not be set to less than 10.
     * Content will be truncated for the summary after <code>SUMMARY_LENGTH</code>
     * characters of plaintext.
     */
    private int SUMMARY_LENGTH = 100;

    @SyndicationElement(type = FeedType.RSS, name = "title")
    public String getTitle() {
        return getProperty("website:title");
    }
    
    public HippoHtml getHtml(){
        return getHippoHtml("website:body");    
    }

    @SyndicationElement(type = FeedType.RSS, name = "author")
    public String getAuthor() {
        return getProperty("website:author");
    }
    
    /**
     * Get the imageset of the newspage
     *
     * @return the imageset of the newspage
     */
    public HippoGalleryImageSetBean getImage() {
        return getLinkedBean("website:image", HippoGalleryImageSetBean.class);
    }
    
    public boolean getCommentsAllowed() {
      Boolean commentsAllowed = getProperty("website:commentsAllowed");
      if (commentsAllowed != null) {
          return commentsAllowed.booleanValue();
      }
      return false;
    }

    /**
     * Get the summary of the blog content. Content will be truncated
     * at {@link AbstractMethodError#SUMMARY_LENGTH}.
     * @return Summarized blog content.
     */
    @SyndicationElement(type = FeedType.RSS, name = "description")
    public Description getSummary() {
    	Description ret = new Description();
        String parsedContent = Jsoup.parse(getHippoHtml("website:body").getContent()).text();
        String summary = "";
        
        if (parsedContent.length() > SUMMARY_LENGTH) {
            int indexOfLastSpace = parsedContent.lastIndexOf(' ', SUMMARY_LENGTH);
            if (indexOfLastSpace < 10) {
                //Have at least 10 characters in summary
                indexOfLastSpace = 10;
            }
            
            summary = parsedContent.substring(0, indexOfLastSpace) + "...";
            
        } else {
        	summary = parsedContent;
        }
        
        ret.setValue(summary);
        ret.setType(null);
        
        return ret;
    }
    
    /**
     * Get the date of the blog entry. This is the publication date of
     * the entry.
     * @return The publication date of the blog entry.
     */
    @SyndicationElement(type = FeedType.RSS, name = "pubDate")
    public Date getDate() {
        GregorianCalendar cal = getProperty("hippostdpubwf:publicationDate");
        return cal.getTime();
    }
    
    @SyndicationElement(type = FeedType.RSS, name = "link")
    public String getSyndicationLink() {
    	final HstRequestContext hstRequestContext = RequestContextProvider.get();
        return hstRequestContext.getHstLinkCreator().create(this, hstRequestContext).toUrlForm(hstRequestContext, true);
    }

    @SyndicationElement(type = FeedType.RSS, name = "guid")
    public Guid getGuid() {
    	Guid ret = new Guid();
    	
    	ret.setPermaLink(true);
    	ret.setValue(this.getSyndicationLink());
    	
    	return ret;
    }

    @Override
    public void update(BaseDocumentRepresentation representation) throws RepositoryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

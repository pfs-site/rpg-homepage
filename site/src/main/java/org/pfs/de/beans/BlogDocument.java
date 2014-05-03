package org.pfs.de.beans;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.jcr.RepositoryException;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSetBean;
import org.htmlcleaner.HtmlCleaner;
import org.jsoup.Jsoup;
import org.pfs.de.services.model.BaseDocumentRepresentation;

@Node(jcrType="website:blogdocument")
public class BlogDocument extends BaseDocument{
    /**
     * Maximum length of the summary. Must not be set to less than 10.
     * Content will be truncated for the summary after <code>SUMMARY_LENGTH</code>
     * characters of plaintext.
     */
    private int SUMMARY_LENGTH = 100;

    public String getTitle() {
        return getProperty("website:title");
    }
    
    public HippoHtml getHtml(){
        return getHippoHtml("website:body");    
    }

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
    public String getSummary() {
        String parsedContent = Jsoup.parse(getHippoHtml("website:body").getContent()).text();
        
        if (parsedContent.length() > SUMMARY_LENGTH) {
            int indexOfLastSpace = parsedContent.lastIndexOf(' ', SUMMARY_LENGTH);
            if (indexOfLastSpace < 10) {
                //Have at least 10 characters in summary
                indexOfLastSpace = 10;
            }
            
            return parsedContent.substring(0, indexOfLastSpace) + "...";
            
        } else {
            return parsedContent;
        }
    }
    
    /**
     * Get the date of the blog entry. This is the publication date of
     * the entry.
     * @return The publication date of the blog entry.
     */
    public Date getDate() {
        GregorianCalendar cal = getProperty("hippostdpubwf:publicationDate");
        return cal.getTime();
    }

    @Override
    public void update(BaseDocumentRepresentation representation) throws RepositoryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

package org.pfs.de.beans;

import java.util.Date;
import java.util.GregorianCalendar;
import javax.jcr.RepositoryException;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.pfs.de.services.model.BaseDocumentRepresentation;

@Node(jcrType="website:textdocument")
public class TextDocument extends BaseDocument{
    
    public String getTitle() {
        return getProperty("website:title");
    }

    public HippoHtml getHtml(){
        return getHippoHtml("website:body");
    }

    /**
     * Get the date of the document. This is the publication date of
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

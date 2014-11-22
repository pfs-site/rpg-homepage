package org.pfs.de.beans;

import java.util.Calendar;
import javax.jcr.RepositoryException;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSetBean;
import org.pfs.de.services.model.BaseDocumentRepresentation;

@Node(jcrType="website:newsdocument")
public class NewsDocument extends BaseDocument{

    public String getTitle() {
        return getProperty("website:title");
    }
    
    public String getSummary() {
        return getProperty("website:summary");
    }
    
    public Calendar getDate() {
        return getProperty("website:date");
    }

    public HippoHtml getHtml(){
        return getHippoHtml("website:body");    
    }

    /**
     * Get the imageset of the newspage
     *
     * @return the imageset of the newspage
     */
    public HippoGalleryImageSetBean getImage() {
        return getLinkedBean("website:image", HippoGalleryImageSetBean.class);
    }

    @Override
    public void update(BaseDocumentRepresentation representation) throws RepositoryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}

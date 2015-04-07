package org.pfs.de.beans;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.content.beans.standard.HippoMirror;
import org.pfs.de.services.model.BaseDocumentRepresentation;

import javax.jcr.RepositoryException;
import java.util.List;

/**
 * Created by jpralle on 03.04.2015.
 */
@Node(jcrType = "website:bannerdocument")
public class BannerDocument extends BaseDocument {

    protected static final String FIELD_IMPRINT = "website:imprint";
    protected static final String FIELD_CONTACT = "website:contact";
    protected static final String FIELD_RSS = "website:rss";

    /**
     * Get the referenced document for the imprint link.
     *
     * @return The document that was set for the imprint link to reference to.
     */
    public HippoDocument getImprintDocument() {
        return getDocument(FIELD_IMPRINT);
    }

    /**
     * Get the referenced document for the contact link.
     *
     * @return The document that was set for the contact link to reference to.
     */
    public HippoDocument getContactDocument() {
        return getDocument(FIELD_CONTACT);
    }

    /**
     * Get the referenced document for the RSS link.
     *
     * @return The document that was set for the RSS link to reference to.
     */
    public HippoDocument getRssDocument() {
        return getDocument(FIELD_RSS);
    }

    private HippoDocument getDocument(String fieldName) {
        HippoDocument ret = null;
        List<Object> docs = getChildBeansByName(fieldName);
        HippoMirror mirror = null;
        HippoBean bean = null;
        if ((docs != null) && (docs.size() > 0) && (docs.get(0) instanceof HippoMirror)) {
            mirror = (HippoMirror) docs.get(0);
            if (mirror != null) {
                bean = mirror.getReferencedBean();
            }
        }
        if ((bean != null) && (bean.isHippoDocumentBean())) {
            ret = (HippoDocument) bean;
        }
        return ret;
    }

    /**
     * Update this document bean with the data of the document representation.
     *
     * @param representation The document representation.
     * @throws UnsupportedOperationException Always thrown by the default implementation.
     */
    @Override
    public void update(BaseDocumentRepresentation representation) throws RepositoryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

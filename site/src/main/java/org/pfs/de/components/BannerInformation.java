package org.pfs.de.components;

import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.configuration.hosting.Mount;
import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.content.beans.ObjectBeanManagerException;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.pfs.de.beans.BannerDocument;
import org.pfs.de.channels.WebsiteInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class BannerInformation extends BaseComponent {

    public static final Logger log = LoggerFactory.getLogger(BannerInformation.class);

    @Override
    public void doBeforeRender(final HstRequest request, final HstResponse response) throws HstComponentException {

        Mount mount = request.getRequestContext().getResolvedMount().getMount();
        WebsiteInfo info = mount.getChannelInfo();


        BannerDocument document = null;
        try {
            String bannerInfoPath = info.getBannerInformationPath();
            document = (BannerDocument)getSiteContentBaseBean(request).getObjectConverter().getObject(getSiteContentBaseBean(request).getNode().getSession(), bannerInfoPath);
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (ObjectBeanManagerException e) {
			e.printStackTrace();
		}

        if (document == null) {
            log.warn("Did not find a content bean for relative content path '{}' for pathInfo '{}'", 
                         request.getRequestContext().getResolvedSiteMapItem().getRelativeContentPath(),
                         request.getRequestContext().getResolvedSiteMapItem().getPathInfo());
            response.setStatus(404);
            return;
        }
        request.setAttribute("document",document);

    }

}

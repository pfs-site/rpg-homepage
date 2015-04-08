package org.pfs.de.components;

import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.configuration.hosting.Mount;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
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

        Node node = null;
        Node document = null;
        try {
            Session session = request.getRequestContext().getSession();
            String bannerInfoPath = info.getBannerInformationPath();
            node = session.getRootNode().getNode(bannerInfoPath);
            document = node.getNode(node.getName());
        } catch (RepositoryException e) {
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

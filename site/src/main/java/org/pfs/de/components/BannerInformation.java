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

public class BannerInformation extends BaseHstComponent {

    public static final Logger log = LoggerFactory.getLogger(BannerInformation.class);

    @Override
    public void doBeforeRender(final HstRequest request, final HstResponse response) throws HstComponentException {

        int count = 0;
        request.setAttribute("count", count);

        Mount mount = request.getRequestContext().getResolvedMount().getMount();
        WebsiteInfo info = mount.getChannelInfo();

        //Session session = UserSession.get().getJcrSession();
        Repository repository = null;
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context)initCtx.lookup("java:comp/env");
            repository = (Repository) envCtx.lookup("jcr/repository");
        } catch (NamingException e) {
            e.printStackTrace();
        }
        Node node = null;
        Node document = null;
        count = 1;
        request.setAttribute("count", count);
        try {
            Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            count = 2;
            request.setAttribute("count", count);
            String bannerInfoPath = info.getBannerInformationPath();
            count = 3;
            request.setAttribute("count", count);
            node = session.getRootNode().getNode(bannerInfoPath);
            count = 4;
            request.setAttribute("count", count);
            document = node.getNode(node.getName());
            count = 5;
            request.setAttribute("count", count);
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
        request.setAttribute("count", count);

    }

}

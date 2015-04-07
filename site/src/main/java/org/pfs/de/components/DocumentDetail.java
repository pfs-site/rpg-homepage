package org.pfs.de.components;

import java.util.logging.Level;

import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.parameters.ParametersInfo;
import org.pfs.de.componentsinfo.PageableListInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ParametersInfo(type = PageableListInfo.class)
public class DocumentDetail extends BaseComponent {

  public static final Logger log = LoggerFactory.getLogger(DocumentDetail.class);

  @Override
  public void doBeforeRender(final HstRequest request, final HstResponse response) throws HstComponentException {

    HippoBean document = getContentBean(request);
    request.setAttribute("document", document);
    
    if(document == null) {
      log.warn("Did not find a content bean for relative content path '{}' for pathInfo '{}'",
              request.getRequestContext().getResolvedSiteMapItem().getRelativeContentPath(),
              request.getRequestContext().getResolvedSiteMapItem().getPathInfo());
      response.setStatus(404);
      return;
    }
  }

}

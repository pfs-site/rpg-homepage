package org.pfs.de.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.hippoecm.hst.content.beans.query.filter.Filter;
import org.pfs.de.componentsinfo.PageableListInfo;
import org.hippoecm.hst.core.parameters.ParametersInfo;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.pfs.de.beans.BlogDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ParametersInfo(type = PageableListInfo.class)
public class BlogDetail extends BaseComponent {

  public static final Logger log = LoggerFactory.getLogger(BlogDetail.class);

  @Override
  public void doBeforeRender(final HstRequest request, final HstResponse response) throws HstComponentException {

      HippoBean doc = getContentBean(request);

      if (doc == null) {
          log.warn("Did not find a content bean for relative content path '{}' for pathInfo '{}'", 
                       request.getRequestContext().getResolvedSiteMapItem().getRelativeContentPath(),
                       request.getRequestContext().getResolvedSiteMapItem().getPathInfo());
          response.setStatus(404);
          return;
      }
      request.setAttribute("document",doc);

  }

}

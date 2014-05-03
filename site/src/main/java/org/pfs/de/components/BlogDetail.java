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

    HippoBean scope = getContentBean(request);

    if(scope == null) {
      String msg = "For a BlogDetail component there must be a content bean available to search below. Cannot create an detail page";
      java.util.logging.Logger.getLogger(BlogDetail.class.getName()).log(Level.SEVERE, msg);
      response.setStatus(404);
      throw new HstComponentException(msg);
    }

    try {
     //Read all blog entries
     HstQuery query = getQueryManager(request).createQuery(scope, BlogDocument.class);
     query.addOrderByDescending("hippostdpubwf:publicationDate");
     HstQueryResult result = query.execute();
     
     //Set up pagination
     request.setAttribute("result", result);
     
    } catch (QueryException ex) {
      java.util.logging.Logger.getLogger(BlogDetail.class.getName()).log(Level.SEVERE, null, ex);
      response.setStatus(404);
      throw new HstComponentException("Error while creating query. Message: " + ex.getMessage());
    }
    
  }

}

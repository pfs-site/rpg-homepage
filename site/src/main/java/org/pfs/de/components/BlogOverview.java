package org.pfs.de.components;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
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
public class BlogOverview extends BaseComponent {

  public static final Logger log = LoggerFactory.getLogger(BlogOverview.class);

  @Override
  public void doBeforeRender(final HstRequest request, final HstResponse response) throws HstComponentException {

    PageableListInfo info = getComponentParametersInfo(request);
    request.setAttribute("info", info);
    HippoBean scope = getContentBean(request);

    if(scope == null) {
      String msg = "For a BlogOverview component there must be a content bean available to search below. Cannot create an overview";
      java.util.logging.Logger.getLogger(BlogOverview.class.getName()).log(Level.SEVERE, msg);
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
     
     //Parse page parameter
     int page = 1;
     if (request.getParameter("page") != null) {
         try {
            page = Integer.parseInt(request.getParameter("page"));
         } catch (NumberFormatException e) {
             log.warn("Invalid page number", e);
         }
     }
     request.setAttribute("page", page);
     
     //Calculate page count
     int pageCount = result.getSize() / info.getPageSize();
     if (result.getSize() % info.getPageSize() != 0) {
         pageCount += 1;
     }
     
     //Create list with page numbers and calculate at which entry
     //to start the display of entries
     int skipEntries = 0;
     if (pageCount > 1) {
         List<Integer> pages = new ArrayList<Integer>();
         
         for (int index = 1; index <= pageCount; index++) {
             pages.add(index);
         }
         
         request.setAttribute("pages", pages);
         skipEntries = (page - 1) * info.getPageSize();
     }
     request.setAttribute("begin", skipEntries);
     
    } catch (QueryException ex) {
      java.util.logging.Logger.getLogger(BlogOverview.class.getName()).log(Level.SEVERE, null, ex);
      response.setStatus(404);
      throw new HstComponentException("Error while creating query. Message: " + ex.getMessage());
    }
    
  }

}

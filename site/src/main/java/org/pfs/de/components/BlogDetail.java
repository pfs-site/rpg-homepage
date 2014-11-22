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
public class BlogDetail extends BaseComponent {

  public static final Logger log = LoggerFactory.getLogger(BlogDetail.class);

  @Override
  public void doBeforeRender(final HstRequest request, final HstResponse response) throws HstComponentException {

    HippoBean document = getContentBean(request);
    request.setAttribute("document", document);
    
    if(document == null) {
      String msg = "For a BlogDetail component there must be a content bean available to search below. Cannot create an detail page";
      java.util.logging.Logger.getLogger(BlogDetail.class.getName()).log(Level.SEVERE, msg);
      response.setStatus(404);
      throw new HstComponentException(msg);
    }
  }

}

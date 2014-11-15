package org.pfs.de.components;

import java.util.logging.Level;

import javax.jcr.RepositoryException;

import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.hippoecm.hst.content.beans.query.filter.Filter;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.parameters.ParametersInfo;
import org.pfs.de.beans.BlogDocument;
import org.pfs.de.beans.CommentDocument;
import org.pfs.de.componentsinfo.PageableListInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ParametersInfo(type = PageableListInfo.class)
public class Comments extends BaseComponent {

  public static final Logger log = LoggerFactory.getLogger(Comments.class);

  @Override
  public void doBeforeRender(final HstRequest request, final HstResponse response) throws HstComponentException {

    HippoBean document = getContentBean(request);
    if (document instanceof BlogDocument) {
    	request.setAttribute("commentsAllowed", ((BlogDocument)document).getCommentsAllowed());
    	request.setAttribute("referenceDocument", document.getCanonicalUUID());
    } else {
    	request.setAttribute("commentsAllowed", false);
    }
    
    if(document == null) {
      String msg = "For a Comments component there must be a content bean (BlogDocument) available to search below. Cannot create comments";
      java.util.logging.Logger.getLogger(Comments.class.getName()).log(Level.WARNING, msg);
      response.setStatus(404);
      throw new HstComponentException(msg);
    }

    try {
        //Read comments for the selected document
        HstQuery query = getQueryManager(request).createQuery(getSiteContentBaseBean(request), CommentDocument.class);
        query.addOrderByDescending("hippostdpubwf:publicationDate");
        Filter filter = query.createFilter();
         
    	//Comments are linked to parent of current node/blog document.
        String linkedNodeId = document.getNode().getParent().getIdentifier();
	    filter.addEqualTo("website:reference/@hippo:docbase", linkedNodeId.toLowerCase());
	    query.setFilter(filter);
	    HstQueryResult result = query.execute();
	     
	    //Set search results
	    request.setAttribute("comments", result);
	} catch (RepositoryException ex) {
		//Failed search for query is not a fatal error, log and continue
		java.util.logging.Logger.getLogger(Comments.class.getName()).log(Level.WARNING, null, ex);
    } catch (QueryException ex) {
      java.util.logging.Logger.getLogger(Comments.class.getName()).log(Level.WARNING, null, ex);
      response.setStatus(404);
      throw new HstComponentException("Error while creating query. Message: " + ex.getMessage());
    }
    
  }

}

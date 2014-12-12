/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pfs.de.components;

import java.util.List;
import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.sitemenu.HstSiteMenuItem;

/**
 *
 * @author pfs-programmierer
 */
public class Navigation extends BaseHstComponent {
    
    @Override
    public void doBeforeRender(final HstRequest request, final HstResponse response) 
	    throws HstComponentException {
      
      List<HstSiteMenuItem> mainMenu = request.getRequestContext().getHstSiteMenus().getSiteMenu("main").getSiteMenuItems();
      request.setAttribute("mainMenu", mainMenu);
    }
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pfs.de.components;

import java.util.ArrayList;
import org.easymock.EasyMock;
import org.easymock.IMockBuilder;
import org.hippoecm.hst.core.component.HstRequest;
import org.junit.Test;
import org.pfs.de.test.AbstractComponentTest;
import static org.easymock.EasyMock.*;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;

import static org.junit.Assert.*;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoFolder;
import org.hippoecm.hst.core.component.HstComponentException;
import org.pfs.de.beans.BlogDocument;
import org.pfs.de.beans.EventDocument;
import org.pfs.de.componentsinfo.PageableListInfo;

/**
 * Unit tests for the {@link BlogOverview} component.
 * @author pfs-programmierer
 */
public class BlogOverviewTest extends AbstractComponentTest<BlogOverview> {
        
    @Override
    protected Class<BlogOverview> getComponentClass() {
        return BlogOverview.class;
    }
    
    @Override
    protected void setUpMock(IMockBuilder<BlogOverview> mockBuilder) {
        mockBuilder.addMockedMethod("getComponentParametersInfo", HstRequest.class);
        mockBuilder.addMockedMethod("getQueryManager", HstRequest.class);
    }
    
    /**
     * Test that an exception is thrown if no bean is available on the
     * request.
     */
    @Test(expected = HstComponentException.class)
    public void testBeanNotAvailable() {
        replay(componentUnderTest);
        componentUnderTest.doBeforeRender(mockRequest, mockResponse);
    }
    
    /**
     * Set up a test query with 1 result.
     * @param scope Request scope.
     * @return Mocked result.
     * @throws QueryException
     */
    protected HstQueryResult setUpQuery(HippoBean scope) throws QueryException {
    	return setUpQuery(scope, 1);
    }
    
    /**
     * Set up a test query with a specified result size.
     * @param scope Request scope.
     * @param resultSize Number of results.
     * @return Mocked result.
     * @throws QueryException
     */
    @SuppressWarnings("unchecked")
	protected HstQueryResult setUpQuery(HippoBean scope, int resultSize) throws QueryException {
    	HstQueryResult queryResult = createQueryResultMock(resultSize, new Class[] {BlogDocument.class, EventDocument.class });
        setUpQuery(scope, new Class[] {BlogDocument.class, EventDocument.class }, queryResult);
        return queryResult;
    }
    
    /**
     * Test that the query result is set to the request.
     * @throws Exception 
     */
    @Test
    public void testQueryResultsOnRequest() throws Exception {
        
        //Set up the required objects
        HippoBean scope = new HippoFolder();
        HstQueryResult queryResult = setUpQuery(scope);
        PageableListInfo info = EasyMock.createMock(PageableListInfo.class);
        
        //Record method calls
        expect(componentUnderTest.getComponentParametersInfo(mockRequest)).andReturn(info);
        expect(componentUnderTest.getContentBean(mockRequest)).andReturn(scope);
        expect(info.getPageSize()).andStubReturn(10);
        
        //Prepare test
        replay(componentUnderTest, info);
        
        //Test method
        componentUnderTest.doBeforeRender(mockRequest, mockResponse);
        
        //Check metod calls
        verify(componentUnderTest);
        
        //Check objects on request
        assertNotNull("Component parameters info not set", mockRequest.getAttribute("info"));
        assertSame("Incorrect component parameter info in request", info, mockRequest.getAttribute("info"));
        
        assertNotNull("Query result missing not set", mockRequest.getAttribute("result"));
        assertSame("Incorrec query result in request", queryResult, mockRequest.getAttribute("result"));
    }

    /**
     * Test paging with multiple pages.
     * @throws Exception 
     */
	@Test
    public void testPaging() throws Exception {
        
        //Set up the required objects
        HippoBean scope = new HippoFolder();
        setUpQuery(scope, 11);
        PageableListInfo info = EasyMock.createMock(PageableListInfo.class);
        
        //Record method calls
        expect(componentUnderTest.getComponentParametersInfo(mockRequest)).andReturn(info);
        expect(componentUnderTest.getContentBean(mockRequest)).andReturn(scope);
        expect(info.getPageSize()).andStubReturn(5);
        
        //Prepare test
        mockRequest.addParameter("page", "2");
        replay(componentUnderTest, info);
        
        //Test method
        componentUnderTest.doBeforeRender(mockRequest, mockResponse);
        
        //Check metod calls
        verify(componentUnderTest);
        
        //Check paging
        ArrayList<Integer> pages = (ArrayList<Integer>) mockRequest.getAttribute("pages");
        assertNotNull("Pages not set", pages);
        assertEquals("Incorrect page count", 3, pages.size());
        
        Integer begin = (Integer) mockRequest.getAttribute("begin");
        assertNotNull("Begin index not set", begin);
        assertEquals("Incorrect begin index", 5, begin.intValue());
    }
    
    /**
     * Test paging with only a single result page.
     * @throws Exception 
     */
	@Test
    public void testSinglePage() throws Exception {
        
        //Set up the required objects
        HippoBean scope = new HippoFolder();
        setUpQuery(scope);
        PageableListInfo info = EasyMock.createMock(PageableListInfo.class);
        
        //Record method calls
        expect(componentUnderTest.getComponentParametersInfo(mockRequest)).andReturn(info);
        expect(componentUnderTest.getContentBean(mockRequest)).andReturn(scope);
        expect(info.getPageSize()).andStubReturn(5);
        
        //Prepare test
        replay(componentUnderTest, info);
        
        //Test method
        componentUnderTest.doBeforeRender(mockRequest, mockResponse);
        
        //Check metod calls
        verify(componentUnderTest);
        
        //Check paging
        ArrayList<Integer> pages = (ArrayList<Integer>) mockRequest.getAttribute("pages");
        assertNull("Pages set for single page", pages);
        
        Integer begin = (Integer) mockRequest.getAttribute("begin");
        assertNotNull("Begin index not set", begin);
        assertEquals("Incorrect begin index", 0, begin.intValue());
    }
}

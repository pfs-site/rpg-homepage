/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pfs.de.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import org.easymock.IMockBuilder;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryManager;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoBeanIterator;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.mock.content.beans.standard.MockHippoBeanIterator;
import org.hippoecm.hst.mock.core.component.MockHstRequest;
import org.hippoecm.hst.mock.core.component.MockHstResponse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.pfs.de.components.BaseComponent;
import org.pfs.de.components.BlogOverviewTest;
import org.pfs.de.componentsinfo.GeneralListInfo;

/**
 * Abstract base class for tests of components. This class sets
 * up the mock request and response before the tests are started. It
 * also creates the component under test, mocking the method 
 * <code>getContentBean()</code>.
 * @author Martin Dreier
 */
public abstract class AbstractComponentTest<T extends BaseComponent> {
    
    /**
     * Mocked request object.
     */
    protected MockHstRequest mockRequest;
    /**
     * Mocked response object.
     */
    protected MockHstResponse mockResponse;
    
    /**
     * The component under test. Will be created by
     * the {@link #setUp()} method before the tests
     * are executed.
     */
    protected T componentUnderTest;

    /**
     * Set up the request, response, and component
     * under test.
     */
    @Before
    public void setUp() {
                mockRequest = new MockHstRequest();
        mockResponse = new MockHstResponse();
        IMockBuilder<T> mockBuilder = createMockBuilder(getComponentClass()).
                                addMockedMethod("getContentBean", HstRequest.class).
                                addMockedMethod("createAndExecuteSearch", HstRequest.class, GeneralListInfo.class, HippoBean.class, String.class);
        setUpMock(mockBuilder);
        componentUnderTest = mockBuilder.createNiceMock();

    }
    
    /**
     * Set up query. This methods creates mocks for the query manager, query
     * and results, including expected methods. The query is created as a 
     * nice mock. Query manager and query are set to replay.
     * @param scope The scope object to set up the query.
     * @param returnType The query return type. May be <code>null</code>.
     * @param result The expected query result.
     * @return The mocked query.
     */
    protected HstQuery setUpQuery(HippoBean scope, Class<? extends HippoBean> returnType, HstQueryResult result) throws QueryException {
        HstQueryManager queryManager = EasyMock.createMock(HstQueryManager.class);
        HstQuery query = EasyMock.createNiceMock(HstQuery.class);
        
        expect(componentUnderTest.getQueryManager(mockRequest)).andStubReturn(queryManager);
        if (returnType == null) {
            expect(queryManager.createQuery(scope)).andStubReturn(query);
        } else {
            expect(queryManager.createQuery(scope, returnType)).andStubReturn(query);
        }
        expect(query.execute()).andStubReturn(result);
        
        replay(queryManager, query);

        return query;
    }
    
    /**
     * Create a mocked query result.
     * @param resultSize The size of the expected results.
     * @param specificResults Specific documents to include in the results.
     * These documents will be added to the result list first in the order
     * they are listed in the argument list.
     * @return The mocked query. It is set to replay.
     */
    protected <R extends HippoBean> HstQueryResult createQueryResultMock(int resultSize, Class<R> resultType, R... specificResults) {
        
        assertTrue("Number of specific documents is higher than expected result size", resultSize >= specificResults.length);
        
        HstQueryResult result = EasyMock.createMock(HstQueryResult.class);
        
        //Create mocked result list
        List<HippoBean> results = new ArrayList<HippoBean>(resultSize);
        
        //Add specific documents
        results.addAll(Arrays.asList(specificResults));
        while (results.size() < resultSize) {
            //Add generic mocked results
            results.add(EasyMock.createNiceMock(resultType));
        }
        HippoBeanIterator mockedIterator = new MockHippoBeanIterator(results);
        
        //Mock methods
        expect(result.getSize()).andStubReturn(resultSize);
        expect(result.getTotalSize()).andStubReturn(resultSize);
        expect(result.getHippoBeans()).andStubReturn(mockedIterator);
        
        replay(result);
        return result;
    }

    
    /**
     * Get the class of the component under test. Required to
     * set up the moch object.
     * @return The class of the component under test.
     */
    protected abstract Class<T> getComponentClass();
    
    /**
     * Set up the mock builder. This method can be used by subclasses to add
     * additional mocked methods to the mocked object. The default
     * implementation is empty and does not need to be called from 
     * implementers.
     * @param mockBuilder The mock builder object.
     */
    protected void setUpMock(IMockBuilder<T> mockBuilder) {
        //Default implementation is empty
    }
}

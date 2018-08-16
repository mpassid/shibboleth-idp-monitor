/*
 * The MIT License
 * Copyright (c) 2015 CSC - IT Center for Science, http://www.csc.fi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.mpass.shibboleth.profile.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Assert;
import org.mockito.Mockito;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.mpass.shibboleth.monitor.context.MonitoringResultContext;
import fi.mpass.shibboleth.monitor.context.MonitoringSequenceResult;
import fi.mpass.shibboleth.monitor.context.MonitoringSequenceStepResult;
import fi.mpass.shibboleth.profile.impl.StoreMonitoringResult;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.testing.DatabaseTestingSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * Unit tests for {@link StoreMonitoringResult}.
 */
public class StoreMonitoringResultTest {

    /** The action to be tested. */
    protected StoreMonitoringResult action;
    
    /** The datasource used for storing monitoring results. */
    protected DataSource dataSource;

    /** The request context containing the profile context. */
    protected RequestContext src;
    
    /** The profile context containing the relying party context. */
    protected ProfileRequestContext<?, ?> prc;

    private String sequenceId;
    
    private String retryableError;
    
    /**
     * Initialize tests.
     * @throws Exception
     */
    @BeforeMethod
    public void initTests() throws Exception {
        retryableError = "mockRetryableError";
        sequenceId = "mockId";
        dataSource = DatabaseTestingSupport.
                GetMockDataSource("/fi/mpass/shibboleth/storage/MonitoringResultStore.sql", 
                        "MonitoringResultStore");
        action = initAction(dataSource);
    }
    
    public StoreMonitoringResult initAction(final DataSource dataSource) throws Exception {
        action = new StoreMonitoringResult();
        action.setHttpServletResponse(new MockHttpServletResponse());
        populateContext();
        action.setDataSource(dataSource);
        action.setTransactionRetries(5);
        List<String> errors = new ArrayList<String>();
        errors.add(retryableError);
        action.setRetryableErrors(errors);
        action.initialize();
        return action;
    }
    
    /**
     * Empties the database.
     */
    @AfterMethod
    public void tearDown() {
        DatabaseTestingSupport.InitializeDataSource("/fi/mpass/shibboleth/storage/DeleteStore.sql", dataSource);
    }
    
    /**
     * Tests action without attached {@link MonitoringResultContext}.
     * @throws Exception
     */
    @Test
    public void testNoMonitoringContext() throws Exception {
        ActionTestingSupport.assertEvent(action.execute(src), EventIds.INVALID_PROFILE_CTX);
    }

    /**
     * Tests action without any results in the {@link MonitoringResultContext}.
     * @throws Exception
     */
    @Test
    public void testNoMonitoringResults() throws Exception {
        final MonitoringResultContext monitoringCtx = new MonitoringResultContext();
        prc.addSubcontext(monitoringCtx);
        ActionTestingSupport.assertEvent(action.execute(src), EventIds.INVALID_PROFILE_CTX);
    }    
    
    @Test
    public void testOneResult() throws Exception {
        final MonitoringResultContext monitoringCtx = new MonitoringResultContext();
        long startTime = System.currentTimeMillis() - 1000;
        long endTime = System.currentTimeMillis();
        monitoringCtx.addResult(initMonitoringResult(startTime, endTime));
        prc.addSubcontext(monitoringCtx);
        Assert.assertNull(action.execute(src));
        final Connection connection = dataSource.getConnection();
        final PreparedStatement statement = connection.prepareStatement("SELECT * from " + StoreMonitoringResult.TABLE_NAME_MONITORING_RESULTS);
        final ResultSet set = statement.executeQuery();
        assertResult(connection, set, 0, startTime, endTime);
        Assert.assertFalse(set.next());
    }

    @Test
    public void testTwoResults() throws Exception {
        final MonitoringResultContext monitoringCtx = new MonitoringResultContext();
        long startTime1 = System.currentTimeMillis() - 1000;
        long endTime1 = System.currentTimeMillis();
        long startTime2 = System.currentTimeMillis() + 100;
        long endTime2 = System.currentTimeMillis() + 1100;
        monitoringCtx.addResult(initMonitoringResult(startTime1, endTime1));
        monitoringCtx.addResult(initMonitoringResult(startTime2, endTime2));
        prc.addSubcontext(monitoringCtx);
        Assert.assertNull(action.execute(src));
        final Connection connection = dataSource.getConnection();
        final PreparedStatement getResults = connection.prepareStatement("SELECT * from " + StoreMonitoringResult.TABLE_NAME_MONITORING_RESULTS);
        final ResultSet set = getResults.executeQuery();
        assertResult(connection, set, 0, startTime1, endTime1);
        assertResult(connection, set, 1, startTime2, endTime2);
        Assert.assertFalse(set.next());
    }
    
    @Test
    public void testFailedConnection() throws Exception {
        final MonitoringResultContext monitoringCtx = new MonitoringResultContext();
        long startTime = System.currentTimeMillis() - 1000;
        long endTime = System.currentTimeMillis();
        DataSource dataSource = Mockito.mock(DataSource.class);
        Connection connection = Mockito.mock(Connection.class);
        SQLException exception = new SQLException("mock", retryableError);
        Mockito.when(connection.prepareStatement((String)Mockito.any(), Mockito.anyInt())).thenThrow(exception);
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        action = initAction(dataSource);
        monitoringCtx.addResult(initMonitoringResult(startTime, endTime));
        prc.addSubcontext(monitoringCtx);
        ActionTestingSupport.assertEvent(action.execute(src), EventIds.IO_ERROR);
    }

    protected void assertResult(final Connection connection, final ResultSet set, final int id, final long startTime, final long endTime) throws Exception {
        Assert.assertTrue(set.next());
        Assert.assertEquals(id, set.getBigDecimal(1).longValue());
        Assert.assertEquals(startTime, set.getLong("startTime"));
        Assert.assertEquals(endTime, set.getLong("endTime"));
        Assert.assertEquals(sequenceId, set.getString("sourceId"));
        final PreparedStatement getStepResults = connection.prepareStatement("SELECT * from " + StoreMonitoringResult.TABLE_NAME_MONITORING_STEP_RESULTS + " where resultId=" + id);
        final ResultSet stepSet = getStepResults.executeQuery();
        Assert.assertTrue(stepSet.next());
        Assert.assertEquals(startTime, stepSet.getLong("startTime"));
        Assert.assertEquals(endTime, stepSet.getLong("endTime"));
        Assert.assertFalse(stepSet.next());
    }
    
    protected MonitoringSequenceResult initMonitoringResult(long startTime, long endTime) {
        final MonitoringSequenceResult monitoringResult = new MonitoringSequenceResult();
        monitoringResult.setId(sequenceId);
        monitoringResult.setStartTime(startTime);
        monitoringResult.setEndTime(endTime);
        final MonitoringSequenceStepResult step = new MonitoringSequenceStepResult();
        step.setStartTime(startTime);
        step.setEndTime(endTime);
        monitoringResult.addStepResult(step);
        return monitoringResult;
    }
    
    /**
     * Populates the request context together with its relevant subcontexts.
     * 
     * @throws ComponentInitializationException If the contexts cannot be initialized.
     */
    public void populateContext() throws ComponentInitializationException {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
    }
}

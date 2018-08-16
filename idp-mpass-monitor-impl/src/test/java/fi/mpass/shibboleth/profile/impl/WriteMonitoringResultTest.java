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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.mockito.Mockito;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.mpass.shibboleth.monitor.context.MonitoringResultContext;
import fi.mpass.shibboleth.monitor.context.MonitoringSequenceResult;
import fi.mpass.shibboleth.monitor.context.MonitoringSequenceStepResult;
import fi.mpass.shibboleth.profile.impl.WriteMonitoringResult;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * Unit tests for {@link WriteMonitoringResult}.
 */
public class WriteMonitoringResultTest {

    /** The action to be tested. */
    WriteMonitoringResult action;
    
    /** The error message for the monitoring result. */
    String errorMessage;

    /** The request context containing the profile context. */
    protected RequestContext src;
    
    /** The profile context containing the relying party context. */
    protected ProfileRequestContext<?, ?> prc;

    @BeforeMethod
    public void initTests() throws Exception {
        action = new WriteMonitoringResult();
        action.setHttpServletResponse(new MockHttpServletResponse());
        action.initialize();
        populateContext();
        errorMessage = "mockErrorMessage";
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
    
    @Test
    public void testNoContext() throws Exception {
        action.execute(src);
        MockHttpServletResponse httpResponse = (MockHttpServletResponse) action.getHttpServletResponse();
        Assert.assertEquals(httpResponse.getContentAsString(), WriteMonitoringResult.ERROR_MSG_NO_CONTEXT);
    }
    
    @Test
    public void testNoResults() throws Exception {
        prc.addSubcontext(new MonitoringResultContext());
        action.execute(src);
        MockHttpServletResponse httpResponse = (MockHttpServletResponse) action.getHttpServletResponse();
        Assert.assertEquals(httpResponse.getContentAsString(), WriteMonitoringResult.ERROR_MSG_NO_RESULTS);
    }
    
    @Test
    public void testWithError() throws Exception {
        final MonitoringResultContext monitoringCtx = new MonitoringResultContext();
        monitoringCtx.addResult(initSeqResult(errorMessage));
        prc.addSubcontext(monitoringCtx);
        action.execute(src);
        MockHttpServletResponse httpResponse = (MockHttpServletResponse) action.getHttpServletResponse();
        Assert.assertEquals(httpResponse.getContentAsString(), errorMessage);
    }
    
    @Test
    public void testWithoutError() throws Exception {
        final MonitoringResultContext monitoringCtx = new MonitoringResultContext();
        monitoringCtx.addResult(initSeqResult(null));
        prc.addSubcontext(monitoringCtx);
        action.execute(src);
        MockHttpServletResponse httpResponse = (MockHttpServletResponse) action.getHttpServletResponse();
        Assert.assertTrue(httpResponse.getContentAsString().contains("OK:"));
    }
    
    @Test
    public void testIOError() throws Exception {
        HttpServletResponse httpResponse = Mockito.mock(HttpServletResponse.class);
        Mockito.when(httpResponse.getOutputStream()).thenThrow(new IOException("mock"));
        action = new WriteMonitoringResult();
        action.setHttpServletResponse(httpResponse);
        action.initialize();
        Event event = action.execute(src);
        Assert.assertEquals(event.getId(), EventIds.IO_ERROR);
    }
    
    protected MonitoringSequenceResult initSeqResult(final String newErrorMessage) {
        final MonitoringSequenceStepResult stepResult = new MonitoringSequenceStepResult();
        stepResult.setStartTime(System.currentTimeMillis() - 1000);
        stepResult.setEndTime(System.currentTimeMillis());
        stepResult.setErrorMessage(newErrorMessage);
        final MonitoringSequenceResult seqResult = new MonitoringSequenceResult();
        seqResult.addStepResult(stepResult);
        return seqResult;
    }
}

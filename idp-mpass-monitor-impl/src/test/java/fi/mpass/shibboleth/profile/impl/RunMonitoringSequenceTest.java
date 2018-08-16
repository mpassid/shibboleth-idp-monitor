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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.protocol.HttpContext;
import org.mockito.Mockito;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.mpass.shibboleth.monitor.ResponseValidatorException;
import fi.mpass.shibboleth.monitor.SequenceStep;
import fi.mpass.shibboleth.monitor.SequenceStepResolver;
import fi.mpass.shibboleth.monitor.context.MonitoringResultContext;
import fi.mpass.shibboleth.profile.impl.RunMonitoringSequence;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * Unit tests for {@link RunMonitoringSequence}.
 */
public class RunMonitoringSequenceTest {

    RunMonitoringSequence action;
    
    String initialUrl;
    
    String errorMessage;
    
    /** The request context containing the profile context. */
    protected RequestContext src;
    
    /** The profile context containing the relying party context. */
    protected ProfileRequestContext<?, ?> prc;
    
    @BeforeMethod
    public void initTest() throws Exception {
        initialUrl = "mockInitialUrl";
        errorMessage = "mockErrorMessage";
        action = new RunMonitoringSequence();
        action.setInitialUrl(initialUrl);
        populateContext();
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
    
    protected List<SequenceStepResolver> initResolvers(final int amount, final int error, boolean sameId) 
            throws Exception {
        final List<SequenceStepResolver> resolvers = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            SequenceStepResolver resolver = Mockito.mock(SequenceStepResolver.class);
            if (sameId) {
                Mockito.when(resolver.getId()).thenReturn("mock");
            } else {
                Mockito.when(resolver.getId()).thenReturn("mock" + i);                
            }
            if (i == error) {
                Mockito.when(resolver.resolve((HttpContext)Mockito.any(), (SequenceStep)Mockito.any()))
                    .thenThrow(new ResponseValidatorException(errorMessage));
            } else {
                Mockito.when(resolver.resolve((HttpContext)Mockito.any(), (SequenceStep)Mockito.any()))
                    .thenReturn(new SequenceStep());
                
            }
            resolvers.add(resolver);
        }
        return resolvers;
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testError() throws Exception {
        action.setResolvers(initResolvers(6, 3, false));
        action.initialize();
        action.execute(prc);
        final MonitoringResultContext monitoringCtx = prc.getSubcontext(MonitoringResultContext.class);
        Assert.assertNotNull(monitoringCtx);
        Assert.assertEquals(monitoringCtx.getResults().size(), 1);
        Assert.assertEquals(monitoringCtx.getResults().get(0).getStepResults().size(), 4);
        Assert.assertEquals(monitoringCtx.getResults().get(0).getStepResults().get(3).getErrorMessage(), errorMessage);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testSuccess() throws Exception {
        action.setResolvers(initResolvers(6, 7, false));
        action.initialize();
        action.execute(prc);
        final MonitoringResultContext monitoringCtx = prc.getSubcontext(MonitoringResultContext.class);
        Assert.assertNotNull(monitoringCtx);
        Assert.assertEquals(monitoringCtx.getResults().size(), 1);
        Assert.assertEquals(monitoringCtx.getResults().get(0).getStepResults().size(), 6);
        Assert.assertNull(monitoringCtx.getResults().get(0).getStepResults().get(5).getErrorMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSuccessSameId() throws Exception {
        action.setResolvers(initResolvers(6, 7, true));
        action.initialize();
        action.execute(prc);
        final MonitoringResultContext monitoringCtx = prc.getSubcontext(MonitoringResultContext.class);
        Assert.assertNotNull(monitoringCtx);
        Assert.assertEquals(monitoringCtx.getResults().size(), 1);
        Assert.assertEquals(monitoringCtx.getResults().get(0).getStepResults().size(), 1);
        Assert.assertNull(monitoringCtx.getResults().get(0).getStepResults().get(0).getErrorMessage());
    }
}

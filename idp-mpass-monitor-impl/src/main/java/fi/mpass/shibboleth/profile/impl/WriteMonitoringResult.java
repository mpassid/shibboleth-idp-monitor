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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import fi.mpass.shibboleth.monitor.context.MonitoringResultContext;
import fi.mpass.shibboleth.monitor.context.MonitoringSequenceResult;
import fi.mpass.shibboleth.monitor.context.MonitoringSequenceStepResult;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.ActionSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.net.HttpServletSupport;

/**
 * This action writes the one-line result from {@link MonitoringResultContext} to the servlet response.
 */
@SuppressWarnings("rawtypes")
public class WriteMonitoringResult extends AbstractProfileAction {
    
    /** Error message returned when no context is available. */
    public static final String ERROR_MSG_NO_CONTEXT = "ERROR: No context available";

    /** Error message returned when no results are available. */
    public static final String ERROR_MSG_NO_RESULTS = "ERROR: No results available";

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(WriteMonitoringResult.class);
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        log.debug("Initializing");
        super.doInitialize();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    @Nonnull public Event execute(@Nonnull final RequestContext springRequestContext) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        final HttpServletResponse httpResponse = getHttpServletResponse();
        HttpServletSupport.addNoCacheHeaders(httpResponse);
        HttpServletSupport.setUTF8Encoding(httpResponse);
        final ProfileRequestContext prc = 
                (ProfileRequestContext) getProfileContextLookupStrategy().apply(springRequestContext);
        final MonitoringResultContext monitoringCtx = prc.getSubcontext(MonitoringResultContext.class, false);
        if (monitoringCtx == null) {
            return writeAndReturn(httpResponse, ERROR_MSG_NO_CONTEXT);            
        }
        final List<MonitoringSequenceResult> results = monitoringCtx.getResults();
        if (results == null || results.size() == 0) {
            return writeAndReturn(httpResponse, ERROR_MSG_NO_RESULTS);
        }
        long start = results.get(results.size() - 1).getStartTime();
        for (final MonitoringSequenceStepResult result : results.get(results.size() - 1).getStepResults()) {
            if (result.getErrorMessage() != null) {
                return writeAndReturn(httpResponse, result.getErrorMessage());
            }
        }
        return writeAndReturn(httpResponse, "OK: Full sequence took " + 
                (results.get(results.size() -1).getEndTime() - start) + "ms");
    }
    
    /**
     * Writes the given message to the servlet response and returns a Spring webflow event.
     * @param httpResponse The servlet response.
     * @param message The message to be written to the servlet response.
     * @return The proceed event if the action was successful, IO_ERROR otherwise.
     */
    protected Event writeAndReturn(final HttpServletResponse httpResponse, final String message) {
        try {
            final Writer out = new OutputStreamWriter(httpResponse.getOutputStream(), "UTF-8");
            out.append(message);
            out.flush();
        } catch (IOException e) {
            log.error("{}: Could not encode the JSON response", getLogPrefix(), e);
            httpResponse.setStatus(HttpStatus.SC_SERVICE_UNAVAILABLE);
            return ActionSupport.buildEvent(this, EventIds.IO_ERROR);
        }
        return ActionSupport.buildProceedEvent(this);
    }
}

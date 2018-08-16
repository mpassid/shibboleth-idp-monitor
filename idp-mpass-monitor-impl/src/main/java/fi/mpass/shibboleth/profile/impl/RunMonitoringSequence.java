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

import java.util.List;

import javax.annotation.Nonnull;

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.HttpContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.mpass.shibboleth.monitor.ResponseValidatorException;
import fi.mpass.shibboleth.monitor.SequenceStep;
import fi.mpass.shibboleth.monitor.SequenceStepResolver;
import fi.mpass.shibboleth.monitor.context.MonitoringResultContext;
import fi.mpass.shibboleth.monitor.context.MonitoringSequenceResult;
import fi.mpass.shibboleth.monitor.context.MonitoringSequenceStepResult;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * This actions runs the attached {@link SequenceStepResolver}s.
 */
@SuppressWarnings("rawtypes")
public class RunMonitoringSequence extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(RunMonitoringSequence.class);

    /** The list of attached resolvers. */
    private List<SequenceStepResolver> resolvers;
    
    /** The initial URL for the initial monitoring step. */
    @Nonnull @NotEmpty private String initialUrl;
    
    /** The identifier for the monitoring sequence. */
    @Nonnull @NotEmpty private String sequenceId;
    
    /**
     * Set the list of attached resolvers.
     * @param newResolvers What to set.
     */
    public void setResolvers(List<SequenceStepResolver> newResolvers) {
        resolvers = newResolvers;
    }
    
    /**
     * Set the initial URL for the initial monitoring step.
     * @param url What to set.
     */
    public void setInitialUrl(@Nonnull @NotEmpty final String url) {
        initialUrl = Constraint.isNotEmpty(url, "The initial URL cannot be empty");
    }
    
    /**
     * Set the identifier for the monitoring sequence.
     * @param id What to set.
     */
    public void setSequenceId(@Nonnull @NotEmpty final String id) {
        sequenceId = id;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        log.debug("Initializing");
        super.doInitialize();
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(
            @Nonnull final ProfileRequestContext profileRequestContext) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        final MonitoringResultContext monitoringCtx = 
                profileRequestContext.getSubcontext(MonitoringResultContext.class, true);
        
        final HttpContext context = HttpClientContext.create();
        final CookieStore cookieStore = new BasicCookieStore();
        context.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        final MonitoringSequenceResult seqResult = new MonitoringSequenceResult();
        seqResult.setId(sequenceId);
        seqResult.setStartTime(System.currentTimeMillis());
        SequenceStep initial = new SequenceStep();
        initial.setUrl(initialUrl);
        boolean errorFound = false;
        for (int i = 0; i < resolvers.size() && !errorFound; i++) {
            final SequenceStepResolver resolver = resolvers.get(i);
            final MonitoringSequenceStepResult stepResult;
            final int resultsSize = seqResult.getStepResults().size();
            final boolean editExisting;
            if (i > 0 && resolver.getId().equals(seqResult.getStepResults().get(resultsSize - 1).getId())) {
                stepResult = seqResult.getStepResults().get(resultsSize - 1);
                editExisting = true;
            } else {
                stepResult = new MonitoringSequenceStepResult();
                stepResult.setStartTime(System.currentTimeMillis());
                stepResult.setId(resolver.getId());
                editExisting = false;
            }
            log.debug("Performing step {} : {}", i, initial.toString());
            try {
                initial = resolver.resolve(context, initial);
            } catch (ResponseValidatorException e) {
                log.warn("Response validation failed", e);
                log.trace("The full response was {}", e.getResponseStr());
                stepResult.setErrorMessage(e.getMessage());
                errorFound = true;
            }
            stepResult.setEndTime(System.currentTimeMillis());
            if (editExisting) {
                seqResult.getStepResults().set(resultsSize - 1, stepResult);
            } else {
                seqResult.addStepResult(stepResult);
            }
        }
        seqResult.setEndTime(System.currentTimeMillis());
        monitoringCtx.addResult(seqResult);
    }
}
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

package fi.mpass.shibboleth.monitor;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.protocol.HttpContext;

import fi.mpass.shibboleth.monitor.BaseSequenceStepResolver.SequenceResponse;
import fi.mpass.shibboleth.support.HttpClientBuilder;

/**
 * A {@link SequenceStepResolver} for adding GET-parameters to the previous result.
 */
public class AddParametersResolver extends BaseSequenceStepResolver {
    
    /** The map of GET-parameters to be added. */
    final Map<String, String> getParameters;
    
    /**
     * Constructor.
     * @param clientBuilder The builder for HTTP client.
     */
    public AddParametersResolver(final HttpClientBuilder clientBuilder) {
        this(clientBuilder, null);
    }
    
    /**
     * Constructor.
     * @param clientBuilder The builder for HTTP client.
     * @param parameters The map of GET-parameters to be added.
     */
    public AddParametersResolver(final HttpClientBuilder clientBuilder, final Map<String, String> parameters) {
        super(clientBuilder);
        getParameters = parameters == null ? new HashMap<String, String>() : parameters;
    }

    /** {@inheritDoc} */
    @Override
    public SequenceStep resolve(HttpContext context, SequenceStep startingStep) throws ResponseValidatorException {
        final SequenceStep result = startingStep;
        final StringBuilder urlBuilder = new StringBuilder(startingStep.getUrl());
        for (final String key : getParameters.keySet()) {
            urlBuilder.append("&")
                .append(key)
                .append("=")
                .append(getParameters.get(key));
        }
        result.setUrl(urlBuilder.toString());
        final SequenceResponse response = resolveStep(context, startingStep, isFollowRedirects());
        final String redirectUrl = getHeaderValue(response.getHeaders(), "Location");
        if (!isFollowRedirects() && redirectUrl != null) {
            final SequenceStep resultStep = new SequenceStep();
            resultStep.setUrl(completeUrl(context, redirectUrl));
            return resultStep;
        }
        return result;
    }
}

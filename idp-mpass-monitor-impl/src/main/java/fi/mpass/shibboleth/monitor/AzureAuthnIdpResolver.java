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

import org.apache.http.protocol.HttpContext;

import fi.mpass.shibboleth.monitor.ResponseValidatorException;
import fi.mpass.shibboleth.monitor.SequenceStep;
import fi.mpass.shibboleth.monitor.SequenceStepResolver;
import fi.mpass.shibboleth.support.HttpClientBuilder;

/**
 * A {@link SequenceStepResolver} for resolving the Authentication URL endpoint from Azure.
 */
public class AzureAuthnIdpResolver extends BaseSequenceStepResolver {
    
    /** The key for the Authentication URL. */
    public static final String AUTH_URL_KEY = "AuthURL";

    /**
     * Constructor.
     * @param clientBuilder The builder for HTTP client.
     */
    public AzureAuthnIdpResolver(final HttpClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    /** {@inheritDoc} */
    public SequenceStep resolve(final HttpContext context, final SequenceStep startingStep) 
            throws ResponseValidatorException {
        final String responseStr = resolveStep(context, startingStep, isFollowRedirects()).getResponse();
        final String authUrlKey = "\"" + AUTH_URL_KEY + "\":\"";
        final int keyStartIndex = responseStr.indexOf(authUrlKey);
        if (keyStartIndex < 0) {
            throw new ResponseValidatorException("Could not find '" + authUrlKey + "' from the response!",
                    responseStr);
        }
        final int valueStartIndex = keyStartIndex + authUrlKey.length();
        final int valueEndIndex = responseStr.indexOf("\"", valueStartIndex);
        if (valueEndIndex < 0) {
            throw new ResponseValidatorException("No trailing \" for the '" + authUrlKey + "'", responseStr);
        }
        final SequenceStep resultStep = initResultStep();
        final String authUrl = responseStr.substring(valueStartIndex, valueEndIndex);
        if (authUrl != null) {
            resultStep.setUrl(authUrl);
        }
        return resultStep;
    }
}

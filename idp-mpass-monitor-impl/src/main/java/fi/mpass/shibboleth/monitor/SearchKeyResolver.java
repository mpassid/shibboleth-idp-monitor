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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.mpass.shibboleth.monitor.ResponseValidatorException;
import fi.mpass.shibboleth.monitor.SequenceStep;
import fi.mpass.shibboleth.support.HttpClientBuilder;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * A sequence step resolver that also resolves a parameter value for the resulting step URL as well as values for the
 * given parameter keys.
 */
public class SearchKeyResolver extends BaseSequenceStepResolver {
    
    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(SearchKeyResolver.class);

    /** The key whose value is used as the result step URL. */
    private final String key;
    
    /** The parameter keys which together with values are included in the result step parameters. */
    private final List<String> paramKeys;
    
    /**
     * Constructor.
     * @param searchKey The key whose value is used as the result step URL.
     * @param clientBuilder The HTTP client builder.
     * @param paramSearchKeys The parameter keys which together with values are included in the result step parameters.
     */
    public SearchKeyResolver(final String searchKey, final HttpClientBuilder clientBuilder, 
            final String... paramSearchKeys) {
        super(clientBuilder);
        key = Constraint.isNotEmpty(searchKey, "searchKey cannot be empty!");
        paramKeys = new ArrayList<String>();
        log.debug("Processing the parameter search keys {}", paramSearchKeys.length);
        for (int i = 0; i < paramSearchKeys.length; i++) {
            log.debug("Added param search key {}", paramSearchKeys[i]);
            paramKeys.add(paramSearchKeys[i]);
        }
    }

    /** {@inheritDoc} */
    public SequenceStep resolve(final HttpContext context, final SequenceStep startingStep) 
            throws ResponseValidatorException {
        final String responseStr = resolveStep(context, startingStep, isFollowRedirects()).getResponse();
        if (StringSupport.trimOrNull(responseStr) == null) {
            throw new ResponseValidatorException("Empty response content from the server");
        }
        final SequenceStep resultStep = initResultStep();
        final String url = getValue(responseStr, key);
        if (StringSupport.trimOrNull(url) == null) {
            throw new ResponseValidatorException("Could not find an URL with the key " + key);
        }
        final String normalizedUrl = url.replaceAll("&#x3a;", ":").replaceAll("&#x2f;", "/");
        resultStep.setUrl(completeUrl(context, normalizedUrl));
        log.debug("Starting to process parameter keys {}", paramKeys.size());
        for (final String paramKey : paramKeys) {
            log.debug("Processing parameter key {}", paramKey);
            final String value = getParamValue(responseStr, paramKey);
            if (value != null) {
                log.debug("Found value {} for {}", value, paramKey);
                resultStep.getParameters().add(new BasicNameValuePair(paramKey, value));
            }
        }
        return resultStep;
    }
}

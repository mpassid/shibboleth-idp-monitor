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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.mpass.shibboleth.monitor.ResponseValidatorException;
import fi.mpass.shibboleth.monitor.SequenceStep;
import fi.mpass.shibboleth.support.HttpClientBuilder;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * A sequence step resolver that expects the result to contain an HTML FORM with defined action URL.
 */
public class FormPostTargetResolver extends BaseSequenceStepResolver {
    
    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(FormPostTargetResolver.class);

    /** The automatically parsed FORM parameters. */
    private final List<NameValuePair> parameters;
    
    /** The automatically parsed result FORM parameters. */
    private final List<String> outputParameters;

    /**
     * Constructor.
     * @param clientBuilder The HTTP client builder.
     * @param formItems The FORM parameters that should be parsed from the response.
     */
    public FormPostTargetResolver(final HttpClientBuilder clientBuilder, final String... formItems) {
        this(clientBuilder, null, formItems);
    }
    
    /**
     * Constructor.
     * @param clientBuilder The HTTP client builder.
     * @param initialParams The parameters not existing in the {@link SequenceStep}, but needed for the step.
     * @param formItems The FORM parameters that should be parsed from the response.
     */
    public FormPostTargetResolver(final HttpClientBuilder clientBuilder, final List<NameValuePair> initialParams, 
            final String... formItems) {
        super(clientBuilder);
        parameters = initialParams;
        outputParameters = new ArrayList<>();
        for (final String formItem : formItems) {
            outputParameters.add(formItem);
        }
    }
    
    /** {@inheritDoc} */
    public SequenceStep resolve(final HttpContext context, final SequenceStep startingStep) 
            throws ResponseValidatorException {
        if (parameters != null) {
            log.debug("Adding the step parameters {}", parameters);
            for (final NameValuePair parameter : parameters) {
                startingStep.getParameters().add(parameter);
            }
        }
        final SequenceResponse response = resolveStep(context, startingStep, isFollowRedirects());
        final String redirectUrl = getHeaderValue(response.getHeaders(), "Location");
        if (!isFollowRedirects() && redirectUrl != null) {
            final SequenceStep resultStep = new SequenceStep();
            resultStep.setUrl(completeUrl(context, redirectUrl));
            return resultStep;
        }
        final String result = response.getResponse();
        if (StringSupport.trimOrNull(result) == null) {
            throw new ResponseValidatorException("The response is empty!");
        }
        final SequenceStep resultStep = initResultStep();
        final List<NameValuePair> resultParameters = new ArrayList<>();
        final String action = getValue(result, "action");
        log.debug("Parsed action {}", action);
        for (final String item : outputParameters) {
            final String value = getParamValue(result, item);
            if (value != null) {
                resultParameters.add(new BasicNameValuePair(item, StringEscapeUtils.unescapeHtml(value)));
            }
        }
        if (action != null) {
            final String url = action.replaceAll("&#x3a;", ":").replaceAll("&#x2f;", "/").replace("&amp;", "&");
            resultStep.setUrl(url);
            if (!url.startsWith("http")) {
                final HttpHost target = (HttpHost) context.getAttribute(
                        HttpCoreContext.HTTP_TARGET_HOST);
                resultStep.setUrl(target.getSchemeName() + "://" + target.getHostName() + ":" + target.getPort() + url);
            } else {
                resultStep.setUrl(url);
            }
        }
        if (resultParameters.size() > 0) {
            resultStep.setParameters(resultParameters);
        }
        return resultStep;   
    }
}
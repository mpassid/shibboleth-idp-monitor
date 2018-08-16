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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.mpass.shibboleth.monitor.ResponseValidator;
import fi.mpass.shibboleth.monitor.ResponseValidatorException;
import fi.mpass.shibboleth.monitor.SequenceStep;
import fi.mpass.shibboleth.monitor.SequenceStepResolver;
import fi.mpass.shibboleth.support.HttpClientBuilder;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * The base {@link SequenceStepResolver} implementation.
 */
public abstract class BaseSequenceStepResolver implements SequenceStepResolver {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(BaseSequenceStepResolver.class);

    /** The builder for HTTP client. */
    private final HttpClientBuilder httpClientBuilder;
    
    /** The validators attached to this resolver. */
    private List<ResponseValidator> validators;
    
    /** The identifier for this resolver. */
    private String id;
    
    /** The switch whether or not to follow HTTP redirects automatically. */
    private boolean followRedirects = true;
    
    /** The default result URL if it cannot be resolved dynamically. */
    private String resultUrl;

    /**
     * Constructor.
     * @param clientBuilder The builder for HTTP client.
     */
    public BaseSequenceStepResolver(final HttpClientBuilder clientBuilder) {
        httpClientBuilder = Constraint.isNotNull(clientBuilder, "clientBuilder cannot be null!");
        validators = new ArrayList<ResponseValidator>();
    }

    /** {@inheritDoc} */
    public List<ResponseValidator> getValidators() {
        return validators;
    }
    
    /** {@inheritDoc} */
    public void addValidator(final ResponseValidator validator) {
        validators.add(validator);
    }
    
    /** {@inheritDoc} */
    public void setValidators(List<ResponseValidator> newValidators) {
        validators = newValidators;
    }
    
    /**
     * Set whether or not to automatically follow HTTP redirects.
     * @param value What to set.
     */
    public void setFollowRedirects(boolean value) {
        followRedirects = value;
    }
    
    /**
     * Get whether or not to automatically follow HTTP redirects.
     * @return Whether or not to automatically follow HTTP redirects.
     */
    public boolean isFollowRedirects() {
        return followRedirects;
    }
    
    /**
     * Set the default result URL if it cannot be resolved dynamically.
     * @param url What to set.
     */
    public void setResultUrl(final String url) {
        resultUrl = url;
    }
    
    /**
     * Get default result URL if it cannot be resolved dynamically.
     * @return The default result URL if it cannot be resolved dynamically.
     */
    public String getResultUrl() {
        return resultUrl;
    }
    
    /**
     * Initializes a HTTP client.
     * 
     * @return The HTTP client.
     * @throws ResponseValidatorException If initialization fails for some reason.
     */
    protected synchronized HttpClient initializeHttpClient() throws ResponseValidatorException {
        final HttpClient httpClient;
        try {
            httpClientBuilder.setHttpFollowRedirects(followRedirects);
            httpClient = httpClientBuilder.buildClient();
        } catch (Exception e) {
            log.error("Could not initialize a http client", e);
            throw new ResponseValidatorException(getId() + ": Could not initialize HttpClient!");
        }
        return httpClient;
    }
    
    /**
     * Initializes the HTTP request for the given step.
     * 
     * @param step The SSO sequence step.
     * @return The HTTP request corresponding to the step.
     * @throws ResponseValidatorException If initialization failed for some reason.
     */
    protected HttpUriRequest initializeHttpRequest(final SequenceStep step) throws ResponseValidatorException {
        if (step.getUrl() == null) {
            log.error("The starting step does not contain URL");
            throw new ResponseValidatorException(getId() + ": The starting step does not contain URL");
        }
        final HttpUriRequest request;
        final RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(isFollowRedirects()).build();
        if (step.getParameters() == null || step.getParameters().size() == 0) {
            request = RequestBuilder.get().setUri(step.getUrl()).setConfig(config).build();
        } else {
            try {
                request = RequestBuilder.post().setUri(step.getUrl()).setEntity(
                        new UrlEncodedFormEntity(step.getParameters())).setConfig(config).build();
            } catch (UnsupportedEncodingException e) {
                log.error("Could not encode the given parameters to POST", e);
                throw new ResponseValidatorException(getId() + ": Could not encode the request parameters!");
            }
        }
        log.debug("Successfully built a request to URI {}", request.getURI());
        return request;
    }
    
    /**
     * Resolves the step.
     * 
     * @param context The context containing for instance cookies.
     * @param step The SSO sequence step starting the resolution.
     * @param followRedirect Whether to automatically follow redirects.
     * @return The resulting step.
     * @throws ResponseValidatorException If validation failed for some reason.
     */
    public SequenceResponse resolveStep(final HttpContext context, final SequenceStep step,
            final boolean followRedirect) 
            throws ResponseValidatorException {
        final HttpClient httpClient = initializeHttpClient();
        final HttpUriRequest request = initializeHttpRequest(step);
        final HttpResponse response;
        
        try {
            response = httpClient.execute(request, context);
            if (log.isTraceEnabled()) {
                for (final Header header : response.getAllHeaders()) {
                    log.trace("Header: {} = {}", header.getName(), header.getValue());
                }            
            }
            try {
                if (followRedirect && response.getHeaders("Location") != null 
                        && response.getHeaders("Location").length > 0) {
                    log.trace("Following redirect automatically");
                    final SequenceStep redirectStep = new SequenceStep();
                    final String url = response.getHeaders("Location")[0].getValue();
                    log.debug("Found a value for Location-header: {}", url);
                    if (!url.contains("://")) {
                        final HttpHost target = (HttpHost) context.getAttribute(
                                HttpCoreContext.HTTP_TARGET_HOST);
                        redirectStep.setUrl(target.getSchemeName() + "://" + target.getHostName() + url);
                    } else {
                        redirectStep.setUrl(url);
                    }
                    return resolveStep(context, redirectStep, followRedirect);
                } else {
                    final String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                    for (final ResponseValidator validator : getValidators()) {
                        validator.validate(response, result);
                    }
                    log.trace("Full contents of the response {}", result);
                    return new SequenceResponse(result, response.getAllHeaders());
                }
            } finally {
                EntityUtils.consume(response.getEntity());                
            }
        } catch (IOException e) {
            log.error("Could not perform a http request to {}", request.getURI(), e);
            throw new ResponseValidatorException(getId() + ": Could not perform a http request to "
                    + request.getURI(), e);
        }
    }
    
    /** {@inheritDoc} */
    public String getId() {
        return id;
    }
    
    /**
     * Set the identifier for the resolver.
     * 
     * @param newId What to set.
     * @return The identifier for the resolver.
     */
    public String setId(final String newId) {
        id = Constraint.isNotEmpty(newId, "id cannot be null!");
        return id;
    }
    
    /**
     * Get the paramater value from a given query string.
     * 
     * @param string The query string.
     * @param key The parameter key.
     * @return The parameter value, null if does not exist.
     */
    protected String getValue(final String string, final String key) {
        int index = string.indexOf(key + "=\"");
        log.trace("Found index: {} for key {}", index, key);
        int offset = index + new String(key + "=\"").length();
        if (index != -1) {
            return string.substring(offset, string.indexOf("\"", offset));
        }
        return null;
    }

    /**
     * Get the parameter value for a given key from a given string. The logic is to find the value -attribute in the
     * same XML element as where the key string is located in quotes.
     * 
     * @param string The source string.
     * @param paramKey The key in quotes.
     * @return The contents of the value attribute, null if does not exist.
     */
    protected String getParamValue(final String string, final String paramKey) {
        int index = string.indexOf("\"" + paramKey + "\"");
        if (index < 0) {
            return null;
        }
        int elementEnd = string.indexOf(">", index);
        final String valueKey = "value=\"";
        int valueStart = string.indexOf(valueKey, index);
        if (valueStart > 0 && valueStart < elementEnd) {
            int offset = valueStart + new String(valueKey).length();
            return string.substring(offset, string.indexOf("\"", offset));
        }
        return null;
    }
    
    /**
     * Returns a desired header from the array of headers.
     * @param headers The array of headers.
     * @param headerName The name of the desired header.
     * @return The value of the desired header, or null if not exists in the array.
     */
    protected String getHeaderValue(final Header[] headers, final String headerName) {
        log.trace("Trying to find header {} from {} headers", headerName, headers.length);
        for (final Header header : headers) {
            if (headerName.equals(header.getName())) {
                log.trace("Found value {} for {}", header.getValue(), headerName);
                return StringSupport.trimOrNull(header.getValue());
            }
        }
        log.trace("Could not find a value for {}", headerName);
        return null;
    }
    
    /**
     * Initializes the resulting {@link SequenceStep}. If the default resultUrl has been set, it will
     * be set to the resulting step.
     * @return The resulting {@link SequenceStep}.
     */
    protected SequenceStep initResultStep() {
        final SequenceStep resultStep = new SequenceStep();
        if (getResultUrl() != null) {
            resultStep.setUrl(getResultUrl());
        }
        return resultStep;
    }
    
    /**
     * Adds the scheme and host to the given URL, if it's missing.
     * @param context The HTTP context.
     * @param sourceUrl The URL to be completed.
     * @return The completed URL.
     */
    protected String completeUrl(final HttpContext context, final String sourceUrl) {
        if (!sourceUrl.contains("://")) {
            final HttpHost target = (HttpHost) context.getAttribute(
                    HttpCoreContext.HTTP_TARGET_HOST);
            final String scheme = target.getSchemeName();
            final int port = target.getPort();
            if (port > 0 && (("https".equals(scheme) && port != 443) || ("http".equals(scheme) && port != 80))) {
                return scheme + "://" + target.getHostName() + ":" + port + sourceUrl;                
            }
            return scheme + "://" + target.getHostName() + sourceUrl;
        }
        return sourceUrl;
    }
    
    /**
     * Wrapper class containing response body and headers.
     */
    class SequenceResponse {
        
        /** The response body. */
        private final String response;
        
        /** The headers in the response. */
        private final Header[] headers;
        
        /**
         * Constructor.
         * @param responseStr The response body.
         * @param allHeaders The headers in the response.
         */
        public SequenceResponse(final String responseStr, final Header[] allHeaders) {
            response = responseStr;
            headers = allHeaders;
        }
        
        /**
         * Get the response body.
         * @return The response body.
         */
        public String getResponse() {
            return response;
        }
        
        /**
         * Get the headers in the response.
         * @return The headers in the response.
         */
        public Header[] getHeaders() {
            return headers;
        }
    }
}

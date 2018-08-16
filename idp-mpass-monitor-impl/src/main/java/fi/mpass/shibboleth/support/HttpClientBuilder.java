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

package fi.mpass.shibboleth.support;

import javax.annotation.Nonnull;

import org.apache.http.impl.client.DefaultRedirectStrategy;

/**
 * This class extends {@link net.shibboleth.utilities.java.support.httpclient.HttpClientBuilder} by disabling the
 * automatic redirects completely (including internal redirects) if httpFollowRedirects is set to false.
 */
public class HttpClientBuilder extends net.shibboleth.utilities.java.support.httpclient.HttpClientBuilder {
    
    /**
     * Constructor.
     */
    public HttpClientBuilder() {
        super();
    }

    /**
     * Constructor.
     * @param builder the Apache HttpClientBuilder 4.3+ instance over which to layer this builder
     */
    public HttpClientBuilder(@Nonnull org.apache.http.impl.client.HttpClientBuilder builder) {
        super(builder);
    }
    
    /** {@inheritDoc} */
    public void setHttpFollowRedirects(final boolean followRedirects) {
        super.setHttpFollowRedirects(followRedirects);
        if (followRedirects) {
            super.getApacheBuilder().setRedirectStrategy(new DefaultRedirectStrategy());
        } else {
            super.getApacheBuilder().setRedirectStrategy(new NoRedirectStrategy());
        }
    }
    
    /**
     * Never redirecting {@link RedirectStrategy}.
     */
    class NoRedirectStrategy extends DefaultRedirectStrategy {

        /** {@inheritDoc} */
        @Override
        protected boolean isRedirectable(final String method) {
            return false;
        }
    }
}

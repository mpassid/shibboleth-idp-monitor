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

import org.apache.http.NameValuePair;

/**
 * This class represents one step in the SSO sequence.
 */
public class SequenceStep {

    /** The url where this step is started. */
    private String url;
    
    /** The list of POST method parameters. */
    private List<NameValuePair> parameters;
    
    /**
     * Constructor.
     */
    public SequenceStep() {
        parameters = new ArrayList<>();
    }
    
    /**
     * Get the url where this step is started.
     * @return The url where this step is started.
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Set the url where this step is started.
     * @param newUrl What to set.
     */
    public void setUrl(final String newUrl) {
        url = newUrl;
    }
    
    /**
     * Get the list of POST method parameters.
     * @return The list of POST method parameters.
     */
    public List<NameValuePair> getParameters() {
        return parameters;
    }
    
    /**
     * Set the list of POST method parameters.
     * @param newParameters What to set.
     */
    public void setParameters(final List<NameValuePair> newParameters) {
        parameters = newParameters;
    }
    
    /** {@inheritDoc} */
    public String toString() {
        return "URL: " + url + ", parameters: " + parameters;
    }
}

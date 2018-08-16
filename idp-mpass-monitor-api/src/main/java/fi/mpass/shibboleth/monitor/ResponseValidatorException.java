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

/**
 * This exception carries the information about failed response validation, as produced by a
 * {@link ResponseValidator}.
 */
@SuppressWarnings("serial")
public class ResponseValidatorException extends Exception {
    
    /** The response body that caused the exception. */
    private final String responseStr;

    /**
     * Constructor.
     * @param reason The one-line reason for the exception.
     */
    public ResponseValidatorException(final String reason) {
        this(reason, (String) null);
    }

    /**
     * Constructor.
     * @param reason The one-line reason for the exception.
     * @param response The response body that caused the exception.
     */
    public ResponseValidatorException(final String reason, final String response) {
        super(reason);
        responseStr = response;
    }
    
    /**
     * Constructor.
     * @param reason The one-line reason for the exception.
     * @param rootCause The root cause for the exception.
     */
    public ResponseValidatorException(final String reason, final Throwable rootCause) {
        this(reason, null, rootCause);
    }
    
    /**
     * Constructor.
     * @param reason The one-line reason for the exception.
     * @param response The response body that caused the exception.
     * @param rootCause The root cause for the exception.
     */
    public ResponseValidatorException(final String reason, final String response, final Throwable rootCause) {
        super(reason, rootCause);
        responseStr = response;
    }

    /**
     * Get the response body that caused the exception.
     * @return The response body.
     */
    public String getResponseStr() {
        return responseStr;
    }
}

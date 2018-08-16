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

import javax.annotation.Nonnull;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.mpass.shibboleth.monitor.ResponseValidator;
import fi.mpass.shibboleth.monitor.ResponseValidatorException;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * A {@link ResponseValidator} that checks whether the response header contains expected value.
 */
public class HeaderContainsResponseValidator implements ResponseValidator {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(HeaderContainsResponseValidator.class);

    /** The header name to be verified. */
    @Nonnull @NotEmpty private final String headerName;
    
    /** The expected value for the header. */
    @Nonnull @NotEmpty private final String expected;
    
    /**
     * Constructor.
     * @param header The header name to be verified.
     * @param string The expected value for the header.
     */
    public HeaderContainsResponseValidator(final String header, final String string) {
        headerName = Constraint.isNotEmpty(header, "Header name cannot be empty!");
        expected = Constraint.isNotEmpty(string, "Expected string cannot be empty!");
    }
    
    /** {@inheritDoc} */
    @Override
    public void validate(final HttpResponse httpResponse, final String contents) throws ResponseValidatorException {
        for (final Header header : httpResponse.getHeaders(headerName)) {
            final String value = StringSupport.trimOrNull(header.getValue());
            log.trace("Header {} has value {}", headerName, value);
            if (value != null && value.contains(expected)) {
                log.debug("Header {} value {} contains the expected {}", headerName, value, expected);
                return;
            }
        }
        log.warn("Header {} values did not contain the expected {}", headerName, expected);
        throw new ResponseValidatorException("No header " + headerName + " containing " + expected + " found!");
    }
}
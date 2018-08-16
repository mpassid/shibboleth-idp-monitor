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

import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.mpass.shibboleth.monitor.ResponseValidator;
import fi.mpass.shibboleth.monitor.ResponseValidatorException;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * A {@link ResponseValidator} that checks whether the given string exists in the contents.
 */
public class ContentContainsResponseValidator implements ResponseValidator {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ContentContainsResponseValidator.class);
    
    /** The string expected to be found in the contents. */
    @Nonnull @NotEmpty private final String expected;
    
    /**
     * Constructor.
     * @param string The string expected to be found in the contents.
     */
    public ContentContainsResponseValidator(final String string) {
        expected = Constraint.isNotEmpty(string, "Expected string cannot be empty!");
    }
    
    /** {@inheritDoc} */
    @Override
    public void validate(final HttpResponse httpResponse, final String contents) throws ResponseValidatorException {
        if (contents == null || !contents.contains(expected)) {
            log.debug("{} not included in the response", expected);
            log.trace("The full content was {}", contents);
            throw new ResponseValidatorException("Expected string '" + expected + "' missing!", contents);
        }
    }
}

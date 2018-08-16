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

import java.util.List;

import org.apache.http.protocol.HttpContext;

/**
 * This interface represents a resolver for a step in the SSO sequence.
 */
public interface SequenceStepResolver {

    /**
     * Resolves a step in the SSO sequence.
     * @param context The context containing for instance cookies.
     * @param startingStep The SSO sequence step starting the resolution.
     * @return The resulting step.
     * @throws ResponseValidatorException If validation failed for some reason.
     */
    public SequenceStep resolve(final HttpContext context, final SequenceStep startingStep) 
            throws ResponseValidatorException;
    
    /**
     * Get validators attached to this resolver.
     * @return The list of validators.
     */
    public List<ResponseValidator> getValidators();
    
    /**
     * Add a new validator to this resolver.
     * @param validator What to add.
     */
    public void addValidator(final ResponseValidator validator);

    /**
     * Get the identifier for this resolver.
     * @return The identifier for this resolver.
     */
    public String getId();
}

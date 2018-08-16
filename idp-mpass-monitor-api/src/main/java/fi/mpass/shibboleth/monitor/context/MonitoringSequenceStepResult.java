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

package fi.mpass.shibboleth.monitor.context;

import fi.mpass.shibboleth.monitor.ResponseValidatorException;

/**
 * Class containing result information about one SSO sequence step.
 */
public class MonitoringSequenceStepResult extends AbstractTimestampedResult {

    /** The identifier for the step. */
    private String id;
    
    /** The phase identifier for the step. */
    private int phaseId;

    /** The one-line error message for the step, if any. */
    private String errorMessage;
    
    /** The full cause for the error, if any. */
    private ResponseValidatorException validatorException;

    /**
     * Get the identifier for the step.
     * @return The identifier for the step.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the identifier for the step.
     * @param newId What to set.
     */
    public void setId(String newId) {
        this.id = newId;
    }
    
    /**
     * Get the phase identifier for the step.
     * @return The phase identifier for the step.
     */
    public int getPhaseId() {
        return phaseId;
    }
    
    /**
     * Set the phase identifier for the step.
     * @param newPhaseId What to set.
     */
    public void setPhaseId(int newPhaseId) {
        this.phaseId = newPhaseId;
    }

    /**
     * Get the one-line error message for the step.
     * @return The one-line error message for the step, or null if no error.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Set the one-line error message for the step.
     * @param newErrorMessage What to set.
     */
    public void setErrorMessage(final String newErrorMessage) {
        this.errorMessage = newErrorMessage;
    }
    
    /**
     * Get the full cause for the error.
     * @return The full cause for the error, or null if no error.
     */
    public ResponseValidatorException getValidatorException() {
        return validatorException;
    }
    
    /**
     * Set the full cause for the error.
     * @param exception What to set.
     */
    public void setResponseValidatorException(final ResponseValidatorException exception) {
        this.validatorException = exception;
    }
}

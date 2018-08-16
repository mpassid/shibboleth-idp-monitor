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

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing results of each step for one monitoring sequence result.
 */
public class MonitoringSequenceResult extends AbstractTimestampedResult {
    
    /** The identifier for the monitored sequence. */
    private String id;

    /** The list of step results for one sequence. */
    private final List<MonitoringSequenceStepResult> stepResults;
    
    /**
     * Constructor.
     */
    public MonitoringSequenceResult() {
        stepResults = new ArrayList<>();
    }

    /**
     * Get the identifier for the monitored sequence.
     * @return The identifier for the monitored sequence.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the identifier for the monitored sequence.
     * @param newId What to set.
     */
    public void setId(String newId) {
        this.id = newId;
    }
    
    /**
     * Get the list of step results for the sequence.
     * @return The list of step results for the sequence.
     */
    public List<MonitoringSequenceStepResult> getStepResults() {
        return stepResults;
    }
    
    /**
     * Add a new step result for the sequence.
     * @param result A new step result for the sequence.
     */
    public void addStepResult(final MonitoringSequenceStepResult result) {
        stepResults.add(result);
    }    
}

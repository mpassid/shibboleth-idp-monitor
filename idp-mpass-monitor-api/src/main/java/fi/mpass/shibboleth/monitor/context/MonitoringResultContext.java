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

import org.opensaml.messaging.context.BaseContext;

/**
 * A context carrying {@link MonitoringSequenceResult}s.
 */
public class MonitoringResultContext extends BaseContext {
    
    /** The list of attached results. */
    private final List<MonitoringSequenceResult> results;
    
    /**
     * Constructor.
     */
    public MonitoringResultContext() {
        results = new ArrayList<>();
    }
    
    /**
     * Get the attached results.
     * @return The list of attached results.
     */
    public List<MonitoringSequenceResult> getResults() {
        return results;
    }
    
    /**
     * Attach a new result.
     * @param result What to attach.
     */
    public void addResult(final MonitoringSequenceResult result) {
        results.add(result);
    }

}

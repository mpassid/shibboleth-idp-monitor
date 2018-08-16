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

/**
 * An abstract class for carrying start and end timestamps.
 */
public abstract class AbstractTimestampedResult {

    /** The start timestamp in milliseconds. */
    private long startTime;
    
    /** The end timestamp in milliseconds. */
    private long endTime;

    /**
     * Get the start time.
     * @return The start time.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Set the start time.
     * @param newStartTime What to set.
     */
    public void setStartTime(long newStartTime) {
        this.startTime = newStartTime;
    }

    /**
     * Get the end time.
     * @return The end time.
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Set the end time.
     * @param newEndTime What to set.
     */
    public void setEndTime(long newEndTime) {
        this.endTime = newEndTime;
    }
}

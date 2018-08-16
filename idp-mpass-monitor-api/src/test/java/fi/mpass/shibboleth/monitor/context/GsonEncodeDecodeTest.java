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

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.Gson;

import fi.mpass.shibboleth.monitor.ResponseValidatorException;
import fi.mpass.shibboleth.monitor.context.MonitoringSequenceResult;
import fi.mpass.shibboleth.monitor.context.MonitoringSequenceStepResult;

/**
 * Unit tests decoding and encoding results to and from JSON.
 */
public class GsonEncodeDecodeTest {
    
    private String id1;
    
    private String id2;
    
    private int phaseId1;
    
    private int phaseId2;
    
    private long seqStart;
    
    private long seqEnd;
    
    private long step1Start;
    
    private long step1End;
    
    private long step2Start;
    
    private long step2End;
    
    @BeforeMethod
    public void init() {
        id1 = "mockId1";
        id2 = "mockId2";
        phaseId1 = 1;
        phaseId2 = 2;
        seqStart = 10000;
        seqEnd = 60000;
        step1Start = 20000;
        step1End = 30000;  
        step2Start = 40000;
        step2End = 50000;
    }

    @Test
    public void testGson() throws Exception {
        Gson gson = new Gson();
        final MonitoringSequenceResult seqResult = new MonitoringSequenceResult();
        seqResult.setId(id1);
        seqResult.setStartTime(seqStart);
        seqResult.setEndTime(seqEnd);
        final MonitoringSequenceStepResult stepResult1 = new MonitoringSequenceStepResult();
        stepResult1.setStartTime(step1Start);
        stepResult1.setEndTime(step1End);
        stepResult1.setId(id1);
        stepResult1.setPhaseId(phaseId1);
        seqResult.addStepResult(stepResult1);
        final MonitoringSequenceStepResult stepResult2 = new MonitoringSequenceStepResult();
        stepResult2.setStartTime(step2Start);
        stepResult2.setEndTime(step2End);
        stepResult2.setId(id2);
        stepResult2.setPhaseId(phaseId2);
        final String errorMessage = "mockError";
        final ResponseValidatorException exception = new ResponseValidatorException("mockError", 
                "mockBody", new IOException("IOError"));
        stepResult2.setErrorMessage(errorMessage);
        stepResult2.setResponseValidatorException(exception);
        seqResult.addStepResult(stepResult2);
        final String json = gson.toJson(seqResult);
        final MonitoringSequenceResult jsonResult = gson.fromJson(json, MonitoringSequenceResult.class);
        Assert.assertEquals(jsonResult.getStartTime(), seqStart);
        Assert.assertEquals(jsonResult.getEndTime(), seqEnd);
        Assert.assertEquals(jsonResult.getId(), id1);
        Assert.assertEquals(jsonResult.getStepResults().size(), 2);
        Assert.assertEquals(jsonResult.getStepResults().get(0).getEndTime(), step1End);
        Assert.assertEquals(jsonResult.getStepResults().get(0).getStartTime(), step1Start);
        Assert.assertEquals(jsonResult.getStepResults().get(0).getId(), id1);
        Assert.assertEquals(jsonResult.getStepResults().get(0).getPhaseId(), phaseId1);
        Assert.assertEquals(jsonResult.getStepResults().get(1).getEndTime(), step2End);
        Assert.assertEquals(jsonResult.getStepResults().get(1).getStartTime(), step2Start);
        Assert.assertEquals(jsonResult.getStepResults().get(1).getId(), id2);
        Assert.assertEquals(jsonResult.getStepResults().get(1).getPhaseId(), phaseId2);
        Assert.assertEquals(jsonResult.getStepResults().get(1).getErrorMessage(), errorMessage);
        Assert.assertEquals(jsonResult.getStepResults().get(1).getValidatorException().getMessage(),
                exception.getMessage());
        Assert.assertEquals(jsonResult.getStepResults().get(1).getValidatorException().getResponseStr(), 
                exception.getResponseStr());
    }
}

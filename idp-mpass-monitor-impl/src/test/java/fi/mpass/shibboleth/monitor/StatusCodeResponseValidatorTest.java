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

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.mpass.shibboleth.monitor.ResponseValidatorException;
import fi.mpass.shibboleth.monitor.StatusCodeResponseValidator;

/**
 * Unit tests for {@link StatusCodeResponseValidator}.
 */
public class StatusCodeResponseValidatorTest extends AbstractResponseValidatorTest {
    
    int status;
 
    @BeforeMethod
    public void initTests() {
        status = 200;
        validator = new StatusCodeResponseValidator(status);
    }

    @Test
    public void testSuccess() throws ResponseValidatorException {
        HttpResponse httpResponse = initMockResponse(status);
        validator.validate(httpResponse, new String());
    }
    
    @Test
    public void testUnexpectedCode() {
        Assert.assertTrue(isExceptionThrown(initMockResponse(status + 1), new String()));
    }

    protected HttpResponse initMockResponse(int statusCode) {
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        StatusLine statusLine = Mockito.mock(StatusLine.class);
        Mockito.when(statusLine.getStatusCode()).thenReturn(statusCode);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(statusLine);
        return httpResponse;
    }
}

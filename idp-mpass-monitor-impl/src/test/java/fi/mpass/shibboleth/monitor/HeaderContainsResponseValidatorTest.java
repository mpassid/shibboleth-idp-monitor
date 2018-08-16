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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.mpass.shibboleth.monitor.HeaderContainsResponseValidator;
import fi.mpass.shibboleth.monitor.ResponseValidatorException;

/**
 * Unit tests for {@link HeaderContainsResponseValidator}.
 */
public class HeaderContainsResponseValidatorTest extends AbstractResponseValidatorTest {
    
    String headerName;
        
    String headerValue;
     
    @BeforeMethod
    public void init() {
        headerName = "mockHeader";
        headerValue = "mockValue";
        validator = new HeaderContainsResponseValidator(headerName, headerValue);
    }

    @Test
    public void testSuccess() throws ResponseValidatorException {
        final Header[] headers = initHeaders(headerName, headerValue);
        validator.validate(initMockResponse(headers), null);
    }
    
    @Test
    public void testNullValue() {
        final Header[] headers = initHeaders(headerName, null);
        Assert.assertTrue(isExceptionThrown(initMockResponse(headers), null));
    }

    @Test
    public void testWrongValue() {
        final Header[] headers = initHeaders(headerName, "mockWrong");
        Assert.assertTrue(isExceptionThrown(initMockResponse(headers), null));
    }

    protected Header initHeader(final String name, final String value) {
        Header header = Mockito.mock(Header.class);
        Mockito.when(header.getValue()).thenReturn(value);
        Mockito.when(header.getName()).thenReturn(name);
        return header;
    }
    
    protected Header[] initHeaders(final String name, final String value) {
        final Header[] headers = new Header[2];
        headers[0] = initHeader("diffMock", "diffValue");
        headers[1] = initHeader(name, value);
        return headers;
    }
    
    protected HttpResponse initMockResponse(final Header[] headers) {
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(httpResponse.getHeaders(headerName)).thenReturn(headers);
        return httpResponse;
    }
}

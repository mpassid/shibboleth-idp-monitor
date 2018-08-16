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

import org.apache.http.client.protocol.HttpClientContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.mpass.shibboleth.monitor.SearchKeyResolver;
import fi.mpass.shibboleth.monitor.SequenceStep;
import fi.mpass.shibboleth.monitor.StatusCodeResponseValidator;
import fi.mpass.shibboleth.support.HttpClientBuilder;

/**
 * Unit tests for {@link SearchKeyResolver}.
 */
public class SearchKeyResolverTest extends AbstractSequenceStepResolverTest {
    
    HttpClientBuilder clientBuilder = new HttpClientBuilder();
    
    String searchKey;
    
    @BeforeMethod
    public void initTests() {
        searchKey = "mockKey";
        resolver = new SearchKeyResolver(searchKey, clientBuilder);
        resolver.addValidator(new StatusCodeResponseValidator(200));
        context = HttpClientContext.create();
        startingStep = initStep();
    }
    
    @Test
    public void testNoUrl() throws Exception {
        startingStep.setUrl(null);
        Assert.assertNull(executeWithServer(resolver, context, startingStep, null));
    }

    @Test
    public void testNullContent() throws Exception {
        Assert.assertNull(executeWithServer(resolver, context, startingStep, null));
    }
    
    @Test
    public void testEmptyContent() throws Exception {
        Assert.assertNull(executeWithServer(resolver, context, startingStep, ""));
    }
    
    @Test
    public void testKeyNotFound() throws Exception {
        final String value = "mockValue";
        Assert.assertNull(executeWithServer(resolver, context, startingStep, "invalidKey=\"" + value + "\""));
    }

    @Test
    public void testKeyFoundNoUrl() throws Exception {
        final String value = "/mockValue";
        final SequenceStep result = 
                executeWithServer(resolver, context, startingStep, searchKey + "=\"" + value + "\"");
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getUrl(), "http://localhost:" + CONTAINER_PORT + value);
    }

    @Test
    public void testKeyFoundWithUrl() throws Exception {
        final String value = "http://localhost:" + CONTAINER_PORT + "/mockValue";
        final SequenceStep result = 
                executeWithServer(resolver, context, startingStep, searchKey + "=\"" + value + "\"");
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getUrl(), value);
    }

    @Test
    public void testKeyFoundWithUrlParams() throws Exception {
        final String value = "http://localhost/mockValue";
        final String param1 = "mockParamName";
        final String param1Value = "mockParamValue";
        final String param2 = "mockAnotherName";
        final String param3 = "yetAnotherName";
        resolver = new SearchKeyResolver(searchKey, clientBuilder, param1, param2, param3);
        final String content = searchKey + "=\"" + value + "\"" 
                + " <item name=\"" + param1 + "\" value=\"" + param1Value + "\">"
                + " <item name=\"" + param3 + "\">";
        final SequenceStep result = executeWithServer(resolver, context, startingStep, content);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getUrl(), value);
        Assert.assertEquals(result.getParameters().size(), 1);
        Assert.assertEquals(result.getParameters().get(0).getValue(), param1Value);
    }
}

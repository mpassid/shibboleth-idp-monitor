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
import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.mpass.shibboleth.monitor.AzureUserRealmResolver;
import fi.mpass.shibboleth.monitor.SequenceStep;
import fi.mpass.shibboleth.support.HttpClientBuilder;

/**
 * Unit tests for {@link AzureUserRealmResolver}.
 */
public class AzureUserRealmResolverTest extends AbstractSequenceStepResolverTest {
    
    String userId;
    
    @BeforeMethod
    public void initTests() {
        HttpClientBuilder clientBuilder = new HttpClientBuilder();
        userId = "mockUser@example.org";
        resolver = new AzureUserRealmResolver(clientBuilder, userId, AzureUserRealmResolver.DEFAULT_API_VERSION, 
                "http://localhost:" + CONTAINER_PORT + "/");
        context = HttpClientContext.create();
        startingStep = initStep();
    }
    
    @Test
    public void testNullContent() throws Exception {
        Assert.assertNull(executeWithServer(resolver, context, startingStep, null));
    }
    
    @Test
    public void testEmptyContent() throws Exception {
        Assert.assertNull(executeWithServer(resolver, context, startingStep, null));
    }
    
    @Test
    public void testWithContent() throws Exception {
        final String stsrequest = "mockValue";
        final String content = "name=\"ctx\" value=\"" + stsrequest + "\"";
        final SequenceStep result = executeWithServer(resolver, context, startingStep, content);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getUrl().contains(AzureUserRealmResolver.PARAM_KEY_STSREQUEST + "=" + stsrequest));
    }
}

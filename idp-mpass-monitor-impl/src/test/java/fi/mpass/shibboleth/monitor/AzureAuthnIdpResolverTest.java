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

import javax.annotation.Nonnull;

import org.apache.http.client.protocol.HttpClientContext;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.mpass.shibboleth.monitor.AzureAuthnIdpResolver;
import fi.mpass.shibboleth.monitor.SequenceStep;
import fi.mpass.shibboleth.support.HttpClientBuilder;

/**
 * Unit tests for {@link AzureAuthnIdpResolver}.
 */
public class AzureAuthnIdpResolverTest extends AbstractSequenceStepResolverTest {
    
    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(AzureAuthnIdpResolverTest.class);

    @BeforeMethod
    public void initTests() {
        HttpClientBuilder clientBuilder = new HttpClientBuilder();
        resolver = new AzureAuthnIdpResolver(clientBuilder);
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
    public void testWrongContent() throws Exception {
        final StringBuffer response = new StringBuffer("{ \"");
        response.append(AzureAuthnIdpResolver.AUTH_URL_KEY + "\":\"mock }");
        Assert.assertNull(executeWithServer(resolver, context, startingStep, response.toString()));
    }
    
    @Test
    public void testExpectedContent() throws Exception {
        final String value = "mockValue";
        final StringBuffer response = new StringBuffer("{ \"");
        response.append(AzureAuthnIdpResolver.AUTH_URL_KEY + "\":\"" + value + "\" }");
        final SequenceStep result = executeWithServer(resolver, context, startingStep, response.toString());
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getUrl(), value);
    }


}

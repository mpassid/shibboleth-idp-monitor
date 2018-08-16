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

import fi.mpass.shibboleth.monitor.FormPostTargetResolver;
import fi.mpass.shibboleth.monitor.SequenceStep;

/**
 * Unit tests for {@link FormPostTargetResolver}.
 */
public class FormPostTargetResolverTest extends AbstractSequenceStepResolverTest {
    
    @BeforeMethod
    public void initTests() {
        context = HttpClientContext.create();
        startingStep = initStep();
        resolver = new FormPostTargetResolver(clientBuilder);
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
    public void testNoAction() throws Exception {
        final SequenceStep result = executeWithServer(resolver, context, startingStep, "mockContent");
        Assert.assertNotNull(result);
        Assert.assertNull(result.getUrl());
    }

    @Test
    public void testWithAction() throws Exception {
        final String action = "/mockAction";
        final String content = "<form action=\"" + action + "\">";
        final SequenceStep result = executeWithServer(resolver, context, startingStep, content);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getUrl(), BASE_URL + action);
        Assert.assertEquals(result.getParameters().size(), 0);
    }

    @Test
    public void testWithActionParameters() throws Exception {
        final String param1 = "mockParam1";
        final String param2 = "mockParam2";
        final String param1value = "mockParam1Value";
        resolver = new FormPostTargetResolver(clientBuilder, param1, param2);
        final String action = "/mockAction";
        final String content = "<form action=\"" + action + "\">"
                + "<input name=\"" + param1 + "\" value=\"" + param1value + "\">"
                + "<input name=\"" + param2 + "\"></form>";
        final SequenceStep result = executeWithServer(resolver, context, startingStep, content);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getUrl(), BASE_URL + action);
        Assert.assertEquals(result.getParameters().size(), 1);
        Assert.assertEquals(result.getParameters().get(0).getValue(), param1value);
    }

}

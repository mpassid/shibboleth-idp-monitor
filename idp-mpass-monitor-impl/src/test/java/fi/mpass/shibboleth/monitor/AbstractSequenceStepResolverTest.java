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

import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.mockito.Mockito;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.SocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import fi.mpass.shibboleth.monitor.SearchKeyResolver;
import fi.mpass.shibboleth.monitor.SequenceStep;
import fi.mpass.shibboleth.monitor.SequenceStepResolver;
import fi.mpass.shibboleth.support.HttpClientBuilder;

/**
 * Unit test base for {@link SequenceStepResolver}s.
 */
public class AbstractSequenceStepResolverTest {

    public static int CONTAINER_PORT = 8997;
    
    public static String BASE_URL = "http://localhost:" + CONTAINER_PORT;

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(AbstractSequenceStepResolverTest.class);
    
    HttpClientBuilder clientBuilder = new HttpClientBuilder();
            
    SequenceStepResolver resolver;
    
    HttpContext context;
    
    SequenceStep startingStep;
 
    protected SequenceStep initStep() {
        final SequenceStep sequenceStep = new SequenceStep();
        sequenceStep.setUrl(BASE_URL);
        return sequenceStep;
    }
    
    @Test
    public void testInitFailure() throws Exception {
        HttpClientBuilder clientBuilder = Mockito.mock(HttpClientBuilder.class);
        Mockito.when(clientBuilder.buildClient()).thenThrow(new Exception("mock"));
        resolver = new SearchKeyResolver("mock", clientBuilder);
        ((SearchKeyResolver)resolver).setId("mockId");
        boolean thrown = false;
        try {
            resolver.resolve(HttpClientContext.create(), new SequenceStep());
        } catch (Exception e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }
    
    /**
     * Executes the resolver with server returning desired content.
     * 
     * @param resolver
     * @param context
     * @param startingStep
     * @param responseContent
     * @return
     * @throws Exception
     */
    protected SequenceStep executeWithServer(final SequenceStepResolver resolver, final HttpContext context, 
            final SequenceStep startingStep, final String responseContent) 
            throws Exception {
        final Container container = new SimpleContainer(responseContent);
        final SocketProcessor server = new ContainerSocketProcessor(container);
        final Connection connection = new SocketConnection(server);
        final SocketAddress address = new InetSocketAddress(CONTAINER_PORT);
        connection.connect(address);
        try {
            return resolver.resolve(context, startingStep);
        } catch (Exception e) {
            log.debug("Catched exception", e);
            return null;
        } finally {
            connection.close();
        }
    }

    /**
     * Simple container implementation.
     */
    class SimpleContainer implements Container {

        final String responseContent;
        
        /**
         * Constructor.
         */
        public SimpleContainer(final String response) {
            responseContent = response;
        }

        @Override
        /** {@inheritDoc} */
        public void handle(Request request, Response response) {
            log.trace("Server got request for {}", request.getTarget());
            try {
                response.setContentType("application/json");
                if (responseContent != null) {
                    IOUtils.copy(new StringReader(responseContent), response.getOutputStream());
                }
                response.setCode(200);
                response.getOutputStream().close();
            } catch (Exception e) {
                log.error("Container-side exception ", e);
            }
        }
    }

}

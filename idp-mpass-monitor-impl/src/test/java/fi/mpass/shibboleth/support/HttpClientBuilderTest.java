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

package fi.mpass.shibboleth.support;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.SocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fi.mpass.shibboleth.support.HttpClientBuilder;

/**
 * Unit tests for {@link HttpClientBuilder}.
 */
public class HttpClientBuilderTest {
    
    public static final int CONTAINER_PORT = 8997;
    
    private String url;
    
    @BeforeMethod
    public void initTests() {
        url = "http://localhost:" + CONTAINER_PORT + "/";
    }

    @Test
    public void testNoFollowRedirect() throws Exception {
        final HttpClientBuilder clientBuilder = 
                new HttpClientBuilder(org.apache.http.impl.client.HttpClientBuilder.create());
        clientBuilder.setHttpFollowRedirects(false);
        final HttpClient client = clientBuilder.buildClient();
        Assert.assertEquals(executeWithServer(client), url);
    }
    
    protected String executeWithServer(final HttpClient httpClient) 
            throws Exception {
        final Container container = new SimpleContainer();
        final SocketProcessor server = new ContainerSocketProcessor(container);
        final Connection connection = new SocketConnection(server);
        final SocketAddress address = new InetSocketAddress(CONTAINER_PORT);
        connection.connect(address);
        try {
            final HttpResponse response = httpClient.execute(new HttpGet(url));
            final Header location = response.getFirstHeader("Location");
            if (location == null) {
                return null;
            } else {
                return location.getValue();
            }
        } catch (Exception e) {
            return null;
        } finally {
            connection.close();
        }
    }
    
    /**
     * Simple container implementation.
     */
    class SimpleContainer implements Container {

        @Override
        /** {@inheritDoc} */
        public void handle(Request request, Response response) {
            try {
                response.setCode(302);
                response.addValue("Location", url);
                response.getOutputStream().close();
            } catch (Exception e) {
            }
        }
    }
}

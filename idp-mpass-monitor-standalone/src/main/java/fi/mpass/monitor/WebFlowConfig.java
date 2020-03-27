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

package fi.mpass.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.webflow.config.AbstractFlowConfiguration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.mvc.builder.MvcViewFactoryCreator;
import org.springframework.webflow.mvc.servlet.FlowHandlerAdapter;
import org.springframework.webflow.mvc.servlet.FlowHandlerMapping;

import net.shibboleth.idp.profile.support.ProfileRequestContextFlowExecutionListener;
import net.shibboleth.utilities.java.support.net.ThreadLocalHttpServletRequestProxy;
import net.shibboleth.utilities.java.support.net.ThreadLocalHttpServletResponseProxy;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

@Configuration
public class WebFlowConfig extends AbstractFlowConfiguration {
 
    @Autowired
    private LocalValidatorFactoryBean localValidatorFactoryBean;
    
    @Value("${flowsDirectory}")
    private String flowsDirectory;

    @Bean({"shibboleth.HttpServletRequest"})
    public ThreadLocalHttpServletRequestProxy getThreadLocalHttpServletRequestProxy() {
        return new ThreadLocalHttpServletRequestProxy();
    }
    
    @Bean({"shibboleth.HttpServletResponse"})
    public ThreadLocalHttpServletResponseProxy getThreadLocalHttpServletResponseProxy() {
        return new ThreadLocalHttpServletResponseProxy();
    }
    
    @Bean
    public FlowDefinitionRegistry flowRegistry() {
        return getFlowDefinitionRegistryBuilder()
                .setBasePath(StringSupport.trimOrNull(flowsDirectory) == null ? "classpath:flows" : "file:" + flowsDirectory)
                .addFlowLocationPattern("/**/*-flow.xml")
                .setFlowBuilderServices(this.flowBuilderServices())
                .build();
    }

    @Bean
    public FlowExecutor flowExecutor() {
        return getFlowExecutorBuilder(flowRegistry())
                .addFlowExecutionListener(new ProfileRequestContextFlowExecutionListener())
                .build();
    }
    
    @Bean
    public FlowBuilderServices flowBuilderServices() {
        return getFlowBuilderServicesBuilder()
                .setViewFactoryCreator(new MvcViewFactoryCreator())
                .setValidator(localValidatorFactoryBean)
                .build();
    }

    @Bean
    public FlowHandlerMapping flowHandlerMapping() {
        FlowHandlerMapping handlerMapping = new FlowHandlerMapping();
        handlerMapping.setOrder(-1);
        handlerMapping.setFlowRegistry(this.flowRegistry());
        return handlerMapping;
    }

    @Bean
    public FlowHandlerAdapter flowHandlerAdapter() {
        FlowHandlerAdapter handlerAdapter = new FlowHandlerAdapter();
        handlerAdapter.setFlowExecutor(this.flowExecutor());
        return handlerAdapter;
    }

}
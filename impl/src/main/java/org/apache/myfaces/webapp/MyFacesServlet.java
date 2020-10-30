/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.webapp;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.context.FacesContext;
import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

/**
 * Derived FacesServlet that can be used for debugging purpose
 * and to fix the Weblogic startup issue (FacesServlet is initialized before ServletContextListener).
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class MyFacesServlet implements Servlet, DelegatedFacesServlet
{
    private static final Logger log = Logger.getLogger(MyFacesServlet.class.getName());

    private final FacesServlet delegate = new FacesServlet();
    
    private FacesInitializer _facesInitializer;
    
    
    public void setFacesInitializer(FacesInitializer facesInitializer) // TODO who uses this method?
    {
        _facesInitializer = facesInitializer;
    }

    @Override
    public void destroy()
    {
        delegate.destroy();
    }

    @Override
    public ServletConfig getServletConfig()
    {
        return delegate.getServletConfig();
    }

    @Override
    public String getServletInfo()
    {
        return delegate.getServletInfo();
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException
    {
        ServletContext servletContext = servletConfig.getServletContext();
        
        if (_facesInitializer == null)
        {
            _facesInitializer = FacesInitializerFactory.getFacesInitializer(servletContext);
        }
        
        // Create startup FacesContext before initializing
        FacesContext facesContext = _facesInitializer.initStartupFacesContext(servletContext);
              
        // Check, if ServletContextListener was already called
        Boolean b = (Boolean)servletContext.getAttribute(StartupServletContextListener.FACES_INIT_DONE);
        if (b == null || b == false)
        {
            if(log.isLoggable(Level.WARNING))
            {
                log.warning("ServletContextListener not yet called");
            }
            _facesInitializer.initFaces(servletConfig.getServletContext());
        }
        
        // Destroy startup FacesContext
        _facesInitializer.destroyStartupFacesContext(facesContext);
        
        delegate.init(servletConfig);
        log.info("MyFacesServlet for context '" + servletConfig.getServletContext().getRealPath("/")
                 + "' initialized.");
    }
    
    @Override
    public void service(ServletRequest request, ServletResponse response) throws IOException, ServletException
    {
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("MyFacesServlet service start");
        }
        delegate.service(request, response);
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("MyFacesServlet service finished");
        }
    }

}

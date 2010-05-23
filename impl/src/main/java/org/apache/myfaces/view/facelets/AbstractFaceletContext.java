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
package org.apache.myfaces.view.facelets;

import java.io.IOException;
import java.util.Iterator;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;

import org.apache.myfaces.view.facelets.tag.jsf.core.AjaxHandler;


/**
 * This class contains methods that belongs to original FaceletContext shipped in
 * facelets code before 2.0, but does not take part from api, so are considered 
 * implementation details. This includes methods related to template handling
 * feature of facelets (called by ui:composition, ui:define and ui:insert).
 * 
 * The methods here are only used by the current implementation and the intention
 * is not expose it as public api.
 * 
 * Aditionally, it also contains methods used by the current implementation for
 * implement new features, like composite components and UniqueIdVendor support.
 * 
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 * 
 * @since 2.0
 */
public abstract class AbstractFaceletContext extends FaceletContext
{    
    /**
     * Return the current FaceletCompositionContext instance from this
     * build.
     * 
     * @return the current FaceletCompositionContext instance
     */
    public abstract FaceletCompositionContext getFaceletCompositionContext();
    
    /**
     * Push the passed TemplateClient onto the stack for Definition Resolution
     * @param client
     * @see TemplateClient
     */
    public abstract void pushClient(TemplateClient client);

    /**
     * Pop the last added pushed TemplateClient
     * @see TemplateClient
     */
    public abstract TemplateManager popClient(TemplateClient client);
    
    /**
     * Pop the last added extended TemplateClient
     * @param client
     */
    public abstract TemplateManager popExtendedClient(TemplateClient client);

    public abstract void extendClient(TemplateClient client);

    /**
     * This method will walk through the TemplateClient stack to resolve and
     * apply the definition for the passed name.
     * If it's been resolved and applied, this method will return true.
     * 
     * @param parent the UIComponent to apply to
     * @param name name or null of the definition you want to apply
     * @return true if successfully applied, otherwise false
     * @throws IOException
     * @throws FaceletException
     * @throws FacesException
     * @throws ELException
     */
    public abstract boolean includeDefinition(UIComponent parent, String name)
            throws IOException, FaceletException, FacesException, ELException;
    
    /**
     * Apply the facelet referenced by a url containing a composite component
     * definition to the current UIComponent. In other words, apply the section
     * composite:implementation in the facelet to the current component.
     * 
     * We need to do this here because DefaultFacelet is the one who has and
     * handle the current FaceletFactory instance.
     * 
     * @param parent
     * @param url
     * @throws IOException
     * @throws FaceletException
     * @throws FacesException
     * @throws ELException
     */
    public abstract void applyCompositeComponent(UIComponent parent, Resource resource)
            throws IOException, FaceletException, FacesException, ELException;

    /**
     * Return a descending iterator containing the ajax handlers to be applied
     * to an specific component that implements ClientBehaviorHolder interface,
     * according to the conditions specified on jsf 2.0 spec section 10.4.1.1.
     * 
     * @since 2.0
     */
    public abstract Iterator<AjaxHandler> getAjaxHandlers();
    
    /**
     * @since 2.0
     */
    public abstract void popAjaxHandlerToStack();
    
    /**
     * @since 2.0
     */
    public abstract void pushAjaxHandlerToStack(AjaxHandler parent);
    
    /**
     * Check if this build is for build composite component metadata
     * 
     * @return
     * @since 2.0
     */
    public abstract boolean isBuildingCompositeComponentMetadata();
    
}

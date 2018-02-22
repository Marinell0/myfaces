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
package org.apache.myfaces.renderkit.html;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.application.ProjectStage;
import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.ListenerFor;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.render.Renderer;
import javax.faces.view.Location;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.context.RequestViewContext;
import org.apache.myfaces.shared.renderkit.JSFAttr;
import org.apache.myfaces.shared.renderkit.RendererUtils;
import org.apache.myfaces.shared.renderkit.html.HTML;
import org.apache.myfaces.shared.renderkit.html.util.ResourceUtils;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;
import org.apache.myfaces.view.facelets.el.CompositeComponentELUtils;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;

/**
 * Renderer used by h:outputStylesheet component 
 * 
 * Note: originally this component required PostBuildComponentTreeOnRestoreViewEvent,
 * but that is no longer true because a tag handler implementing RelocatableResourceHandler
 * interface was associated, so the component is really marked and refreshed.
 * 
 * @since 2.0
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@JSFRenderer(renderKitId = "HTML_BASIC", family = "javax.faces.Output", type = "javax.faces.resource.Stylesheet")
@ListenerFor(systemEventClass = PostAddToViewEvent.class)
public class HtmlStylesheetRenderer extends Renderer implements
    ComponentSystemEventListener
{
    private static final Logger log = Logger.getLogger(HtmlStylesheetRenderer.class.getName());
    
    private static final String IS_BUILDING_INITIAL_STATE = "javax.faces.IS_BUILDING_INITIAL_STATE";
    
    public void processEvent(ComponentSystemEvent event)
    {
        if (event instanceof PostAddToViewEvent)
        {
            UIComponent component = event.getComponent();
            FacesContext facesContext = FacesContext.getCurrentInstance();
            
            Location location = (Location) component.getAttributes().get(CompositeComponentELUtils.LOCATION_KEY);
            if (location != null)
            {
                UIComponent ccParent
                        = CompositeComponentELUtils.getCompositeComponentBasedOnLocation(facesContext, location);
                if (ccParent != null)
                {
                    component.getAttributes().put(
                            CompositeComponentELUtils.CC_FIND_COMPONENT_EXPRESSION,
                            ComponentSupport.getFindComponentExpression(facesContext, ccParent));
                }
            }
            // If this is an ajax request and the view is being refreshed and a PostAddToViewEvent
            // was propagated to relocate this resource, means the header must be refreshed.
            // Note ajax request does not occur on non postback requests.

            if (facesContext.getPartialViewContext().isAjaxRequest() )
            {
                boolean isBuildingInitialState = facesContext.getAttributes().
                    containsKey(IS_BUILDING_INITIAL_STATE);
                // The next condition takes into account the current request is an ajax request. 
                boolean isPostAddToViewEventAfterBuildInitialState = 
                    !isBuildingInitialState ||
                    (isBuildingInitialState && 
                            FaceletViewDeclarationLanguage.isRefreshingTransientBuild(facesContext));
                if (isPostAddToViewEventAfterBuildInitialState)
                {
                    //!(component.getParent() instanceof ComponentResourceContainer)
                    RequestViewContext requestViewContext = RequestViewContext.getCurrentInstance(facesContext);
                    requestViewContext.setRenderTarget("head", true, component);
                }
            }
            facesContext.getViewRoot().addComponentResource(facesContext,
                        component, "head");
        }
    }
    
    @Override
    public boolean getRendersChildren()
    {
        return true;
    }

    @Override
    public void encodeChildren(FacesContext facesContext, UIComponent component)
            throws IOException
    {
        if (facesContext == null)
        {
            throw new NullPointerException("context");
        }
        if (component == null)
        {
            throw new NullPointerException("component");
        }

        Map<String, Object> componentAttributesMap = component.getAttributes();
        String resourceName = (String) componentAttributesMap.get(JSFAttr.NAME_ATTR);
        boolean hasChildren = component.getChildCount() > 0;
        
        if (resourceName != null && (!"".equals(resourceName)) )
        {
            if (hasChildren)
            {
                log.info("Component with resourceName "+ resourceName + 
                        " and child components found. Child components will be ignored.");
            }
        }
        else
        {
            if (hasChildren)
            {
                ResponseWriter writer = facesContext.getResponseWriter();
                writer.startElement(HTML.STYLE_ELEM, component);
                writer.writeAttribute(HTML.TYPE_ATTR, HTML.STYLE_TYPE_TEXT_CSS, null);
                RendererUtils.renderChildren(facesContext, component);
                writer.endElement(HTML.STYLE_ELEM);
            }
            else
            {
                if (!facesContext.isProjectStage(ProjectStage.Production))
                {
                    facesContext.addMessage(component.getClientId(facesContext), 
                            new FacesMessage("Component with no name and no body content, so nothing rendered."));
                }
            }            
        }
    }
    
    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent component)
            throws IOException
    {
        super.encodeEnd(facesContext, component); //check for NP
        
        Map<String, Object> componentAttributesMap = component.getAttributes();
        String resourceName = (String) componentAttributesMap.get(JSFAttr.NAME_ATTR);
        String libraryName = (String) componentAttributesMap.get(JSFAttr.LIBRARY_ATTR);

        if (resourceName == null)
        {
            //log.warn("Trying to encode resource represented by component" + 
            //        component.getClientId() + " without resourceName."+
            //        " It will be silenty ignored.");
            return;
        }
        if ("".equals(resourceName))
        {
            return;
        }
        
        String additionalQueryParams = null;
        int index = resourceName.indexOf('?');
        if (index >= 0)
        {
            additionalQueryParams = resourceName.substring(index + 1);
            resourceName = resourceName.substring(0, index);
        }
        
        Resource resource;
        if (libraryName == null)
        {
            if (ResourceUtils.isRenderedStylesheet(facesContext, libraryName, resourceName))
            {
                //Resource already founded
                return;
            }
            resource = facesContext.getApplication().getResourceHandler()
                    .createResource(resourceName);
        }
        else
        {
            if (ResourceUtils.isRenderedStylesheet(facesContext, libraryName, resourceName))
            {
                //Resource already founded
                return;
            }
            resource = facesContext.getApplication().getResourceHandler()
                    .createResource(resourceName, libraryName);

        }
        
        if (resource == null)
        {
            //no resource found
            log.warning("Resource referenced by resourceName "+ resourceName +
                    (libraryName == null ? "" : " and libraryName " + libraryName) +
                    " not found in call to ResourceHandler.createResource."+
                    " It will be silenty ignored.");
            return;
        }
        else
        {
            if (ResourceUtils.isRenderedStylesheet(facesContext, resource.getLibraryName(), resource.getResourceName()))
            {
                //Resource already founded
                return;
            }

            // Rendering resource
            ResourceUtils.markStylesheetAsRendered(facesContext, libraryName, resourceName);
            ResourceUtils.markStylesheetAsRendered(facesContext, resource.getLibraryName(), resource.getResourceName());
            ResponseWriter writer = facesContext.getResponseWriter();
            writer.startElement(HTML.LINK_ELEM, component);
            writer.writeAttribute(HTML.REL_ATTR, HTML.STYLESHEET_VALUE,null );
            String media = (String) component.getAttributes().get("media");
            //writer.writeAttribute("media", media == null ? "screen" : media ,null );
            if (media != null)
            {
                writer.writeAttribute("media", media ,null );
            }
            writer.writeAttribute(HTML.TYPE_ATTR, 
                    (resource.getContentType() == null ? HTML.STYLE_TYPE_TEXT_CSS
                            : resource.getContentType()) , null);
            String path = resource.getRequestPath();
            if (additionalQueryParams != null)
            {
                path = path + ((path.indexOf('?') >= 0) ? "&amp;" : "?") + additionalQueryParams;
            }
            writer.writeURIAttribute(HTML.HREF_ATTR,
                    facesContext.getExternalContext().encodeResourceURL(path), null);
            writer.endElement(HTML.LINK_ELEM);
        }
    }
}

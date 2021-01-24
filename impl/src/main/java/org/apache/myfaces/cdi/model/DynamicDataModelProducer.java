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

package org.apache.myfaces.cdi.model;

import java.util.Map;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Typed;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.model.DataModel;
import org.apache.myfaces.cdi.util.AbstractDynamicProducer;
import org.apache.myfaces.cdi.util.CDIUtils;

/**
 *
 */
@Typed
public class DynamicDataModelProducer extends AbstractDynamicProducer<Map<Class<?>, Class<? extends DataModel>>>
{
    public DynamicDataModelProducer(BeanManager beanManager, DataModelInfo typeInfo)
    {
        super(beanManager);
        
        FacesDataModelAnnotationLiteral literal = new FacesDataModelAnnotationLiteral(typeInfo.getForClass());

        super.id("" + typeInfo.getForClass())
                .scope(Dependent.class)
                .qualifiers(literal)
                .types(typeInfo.getType(), Object.class)
                .beanClass(Map.class)
                .create(e -> createDataModel(e));
    }

    protected Map<Class<?>,Class<? extends DataModel>> createDataModel(
            CreationalContext<Map<Class<?>,Class<? extends DataModel>>> cc)
    {
        FacesDataModelClassBeanHolder holder = CDIUtils.lookup(getBeanManager(), FacesDataModelClassBeanHolder.class);
        return holder.getClassInstanceToDataModelWrapperClassMap();
    }
}
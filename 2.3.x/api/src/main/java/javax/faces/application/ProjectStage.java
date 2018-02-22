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
package javax.faces.application;

/**
 * @since 2.0
 */
public enum ProjectStage
{
    Development,
    Production,
    SystemTest,
    UnitTest;
    
    public static final String PROJECT_STAGE_JNDI_NAME = "java:comp/env/jsf/ProjectStage";

    /**
     * 
     */
    //@JSFWebConfigParam(defaultValue="Production",
    //        expectedValues="Development, Production, SystemTest, UnitTest",
    //        since="2.0")
    public static final String PROJECT_STAGE_PARAM_NAME = "javax.faces.PROJECT_STAGE";
}

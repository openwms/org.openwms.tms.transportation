/*
 * Copyright 2018 Heiko Scherrer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openwms.tms;

/**
 * A TMSConstants holds general constants of this microservice module.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
public final class TMSConstants {

    /** API root to hit TransportOrders (plural). */
    public static final String ROOT_ENTITIES = "/transportorders";
    /**
     * Bean name of the Jackson ObjectMapper to use. Dissenting from the default bean name to not come in conflict with instantiations done
     * be SpringBoot autoconfiguration.
     */
    public static final String BEAN_NAME_OBJECTMAPPER = "jacksonOM";
}

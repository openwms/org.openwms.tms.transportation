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
package org.openwms.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * A LocationGroup.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
public class LocationGroup implements Target, Serializable {

    @JsonProperty
    private boolean incomingActive = true;
    @JsonProperty
    private String name;

    /* JSON */
    LocationGroup() {
    }

    /**
     * Create a LocationGroup with unique name.
     *
     * @param name The unique name
     */
    public LocationGroup(String name) {
        this.name = name;
    }

    /**
     * Checks whether the Locationgroup is blocked for incoming goods.
     *
     * @return {@literal true} if blocked, otherwise {@literal false}
     */
    public boolean isInfeedBlocked() {
        return !incomingActive;
    }

    public void setIncomingActive(boolean incomingActive) {
        this.incomingActive = incomingActive;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String asString() {
        return name;
    }
}

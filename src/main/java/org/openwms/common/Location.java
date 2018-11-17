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

import com.fasterxml.jackson.annotation.JsonCreator;
import org.ameba.integration.jpa.ApplicationEntity;

import java.io.Serializable;

/**
 * A Location.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
public class Location extends ApplicationEntity implements Target, Serializable {

    private String locationId;
    private boolean incomingActive = true;

    @JsonCreator
    public Location() {
    }

    public String getLocationId() {
        return locationId;
    }

    @JsonCreator
    public Location(String locationId) {
        this.locationId = locationId;
    }

    public boolean isIncomingActive() {
        return incomingActive;
    }

    public void setIncomingActive(boolean incomingActive) {
        this.incomingActive = incomingActive;
    }

    /**
     * Return the {@code locationId}.
     *
     * @return String locationId
     */
    @Override
    public String toString() {
        return locationId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String asString() {
        return locationId;
    }

    public boolean isInfeedBlocked() {
        return !incomingActive;
    }
}

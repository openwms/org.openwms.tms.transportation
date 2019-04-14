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
package org.openwms.tms.api.messages;

import java.io.Serializable;
import java.util.StringJoiner;

/**
 * A TransportOrderMO is a Message Object representing a {@code TransportOrder}.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
public class TransportOrderMO implements Serializable {

    public static enum EventType {
        STARTED;
    }

    private EventType eventType;
    private String pKey;
    private String transportUnitBK;
    private String state;
    private String sourceLocation;
    private String targetLocation;
    private String targetLocationGroup;

    /*~-------------------- Generated methods --------------------*/
    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getpKey() {
        return pKey;
    }

    public void setpKey(String pKey) {
        this.pKey = pKey;
    }

    public String getTransportUnitBK() {
        return transportUnitBK;
    }

    public void setTransportUnitBK(String transportUnitBK) {
        this.transportUnitBK = transportUnitBK;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(String sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public String getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(String targetLocation) {
        this.targetLocation = targetLocation;
    }

    public String getTargetLocationGroup() {
        return targetLocationGroup;
    }

    public void setTargetLocationGroup(String targetLocationGroup) {
        this.targetLocationGroup = targetLocationGroup;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TransportOrderMO.class.getSimpleName() + "[", "]").add("eventType=" + eventType).add("pKey='" + pKey + "'").add("transportUnitBK='" + transportUnitBK + "'").add("state='" + state + "'").add("sourceLocation='" + sourceLocation + "'").add("targetLocation='" + targetLocation + "'").add("targetLocationGroup='" + targetLocationGroup + "'").toString();
    }
}

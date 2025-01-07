/*
 * Copyright 2005-2025 the original author or authors.
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
package org.openwms.tms.impl.redirection;

import org.openwms.tms.TransportOrder;

/**
 * A RedirectVote encapsulates a targetLocationGroup and a targetLocation to vote for as a target.
 *
 * @author Heiko Scherrer
 */
public class RedirectVote extends Vote {

    private final String targetLocation;
    private final String targetLocationGroup;
    private final TransportOrder transportOrder;

    /**
     * Create a new RedirectVote.
     *
     * @param targetLocation The target Location to verify
     * @param targetLocationGroup The target LocationGroup to verify
     * @param transportOrder The TransportOrder to vote for
     */
    public RedirectVote(String targetLocation, String targetLocationGroup, TransportOrder transportOrder) {
        this.targetLocation = targetLocation;
        this.targetLocationGroup = targetLocationGroup;
        this.transportOrder = transportOrder;
    }

    /**
     * Get the targetLocation.
     *
     * @return the LocationId.
     */
    public String getTargetLocation() {
        return targetLocation;
    }

    /**
     * Get the targetLocationGroup.
     *
     * @return the LocationGroup name.
     */
    public String getTargetLocationGroup() {
        return targetLocationGroup;
    }

    /**
     * Get the transportOrder.
     *
     * @return The transportOrder
     */
    public TransportOrder getTransportOrder() {
        return transportOrder;
    }
}
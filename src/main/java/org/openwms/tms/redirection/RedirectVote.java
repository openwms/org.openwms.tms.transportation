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
package org.openwms.tms.redirection;

import org.openwms.tms.TransportOrder;

/**
 * A RedirectVote. Encapsulates a targetLocationGroup and a targetLocation to vote for as
 * targets.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
public class RedirectVote extends Vote {

    private String target;
    private TransportOrder transportOrder;

    /**
     * Create a new RedirectVote.
     *
     * @param target The target destination to verify
     * @param transportOrder The TransportOrder to vote for
     */
    public RedirectVote(String target, TransportOrder transportOrder) {
        this.target = target;
        this.transportOrder = transportOrder;
    }

    /**
     * Get the locationGroup.
     *
     * @return the locationGroup.
     */
    public String getTarget() {
        return target;
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
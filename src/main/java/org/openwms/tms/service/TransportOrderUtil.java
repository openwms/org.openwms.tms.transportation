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
package org.openwms.tms.service;

import org.openwms.tms.TransportOrderState;
import org.openwms.tms.TransportServiceEvent;

/**
 * A TransportOrderUtil.
 *
 * @author <a href="mailto:russelltina@users.sourceforge.net">Tina Russell</a>
 */
final class TransportOrderUtil {

    /**
     * Hide constructor of utility classes.
     */
    private TransportOrderUtil() {
    }

    /**
     * Match a {@link TransportOrderState} to a type of event.
     *
     * @param newState The state to be checked
     * @return the certain type event that matches to newState
     */
    public static TransportServiceEvent.TYPE convertToEventType(TransportOrderState newState) {
        switch (newState) {
            case FINISHED:
                return TransportServiceEvent.TYPE.TRANSPORT_FINISHED;
            case CANCELED:
                return TransportServiceEvent.TYPE.TRANSPORT_CANCELED;
            case INTERRUPTED:
                return TransportServiceEvent.TYPE.TRANSPORT_INTERRUPTED;
            case ONFAILURE:
                return TransportServiceEvent.TYPE.TRANSPORT_ONFAILURE;
            default:
                return TransportServiceEvent.TYPE.TRANSPORT_CANCELED;
        }
    }

}

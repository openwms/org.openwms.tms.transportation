/*
 * Copyright 2005-2022 the original author or authors.
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

import org.springframework.context.ApplicationEvent;

import java.io.Serializable;

import static java.lang.String.format;

/**
 * A TransportServiceEvent.
 *
 * @author <a href="mailto:russelltina@users.sourceforge.net">Tina Russell</a>
 */
public final class TransportServiceEvent extends ApplicationEvent implements Serializable {

    private TYPE type;

    /**
     * All possible types of this event.
     *
     * @author <a href="mailto:russelltina@users.sourceforge.net">Tina Russell</a>
     */
    public enum TYPE {

        /** A TransportOrder has been created. */
        TRANSPORT_CREATED,

        /** TransportOrder has been initialized. */
        INITIALIZED,

        /** TransportOrder has been started. */
        STARTED,

        /** A TransportOrder was interrupted. */
        TRANSPORT_INTERRUPTED,

        /** A TransportOrder was set on failure. */
        TRANSPORT_ONFAILURE,

        /** A TransportOrder was canceled. */
        TRANSPORT_CANCELED,

        /** A TransportOrder was finished. */
        TRANSPORT_FINISHED;

        public static TYPE of(TransportOrderState requestedState) {
            switch (requestedState) {
                case CREATED: return TRANSPORT_CREATED;
                case INITIALIZED: return INITIALIZED;
                case STARTED: return STARTED;
                case INTERRUPTED: return TRANSPORT_INTERRUPTED;
                case CANCELED: return TRANSPORT_CANCELED;
                case ONFAILURE: return TRANSPORT_ONFAILURE;
                case FINISHED: return TRANSPORT_FINISHED;
                default: throw new IllegalStateException(format("The state [%s] is not supported", requestedState));
            }
        }
    }


    /**
     * Create a new RootApplicationEvent.
     *
     * @param source Event source
     */
    public TransportServiceEvent(Object source) {
        super(source);
    }

    /**
     * Create a new TransportServiceEvent.
     *
     * @param source Event source
     * @param type Event type
     */
    public TransportServiceEvent(Object source, TYPE type) {
        super(source);
        this.type = type;
    }

    /**
     * Return the type of event.
     *
     * @return The event type
     */
    public TYPE getType() {
        return type;
    }
}

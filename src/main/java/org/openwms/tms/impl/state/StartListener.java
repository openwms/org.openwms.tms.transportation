/*
 * Copyright 2005-2019 the original author or authors.
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
package org.openwms.tms.impl.state;

import org.ameba.exception.NotFoundException;
import org.openwms.tms.TransportOrder;
import org.openwms.tms.TransportServiceEvent;
import org.openwms.tms.impl.TransportOrderRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * A StartListener.
 *
 * @author Heiko Scherrer
 */
@Service
class StartListener {

    private final TransportOrderRepository repository;
    private final Starter starter;

    StartListener(TransportOrderRepository repository, Starter starter) {
        this.repository = repository;
        this.starter = starter;
    }

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @EventListener
    public void onEvent(TransportServiceEvent event) {
        Long pk = ((TransportOrder) event.getSource()).getPk();
        final TransportOrder to = repository.findById(pk).orElseThrow(NotFoundException::new);
        switch (event.getType()) {
            case INITIALIZED:
                starter.triggerStart(to);
                break;
            case TRANSPORT_FINISHED:
            case TRANSPORT_ONFAILURE:
            case TRANSPORT_CANCELED:
            case TRANSPORT_INTERRUPTED:
                starter.startNext(to);
                break;
            default:
                // just accept the evolution here
        }
    }
}

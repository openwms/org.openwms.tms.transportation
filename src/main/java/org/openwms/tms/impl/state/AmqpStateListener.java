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
package org.openwms.tms.impl.state;

import org.ameba.annotation.Measured;
import org.ameba.exception.ServiceLayerException;
import org.openwms.core.SpringProfiles;
import org.openwms.tms.Message;
import org.openwms.tms.StateChangeException;
import org.openwms.tms.TransportOrder;
import org.openwms.tms.TransportOrderState;
import org.openwms.tms.TransportationService;
import org.openwms.tms.api.requests.state.StateChangeResponse;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

/**
 * An AmqpStateListener listens on a start response from remote services as a reply to a former start request, validates the response and
 * delegates to the {@link Startable} instance for final starting of the {@code TransportOrder}.
 *
 * @author Heiko Scherrer
 */
@Profile(SpringProfiles.ASYNCHRONOUS_PROFILE)
@Component
class AmqpStateListener {

    private final Startable starter;
    private final TransportationService<TransportOrder> service;

    AmqpStateListener(Startable starter, TransportationService<TransportOrder> service) {
        this.starter = starter;
        this.service = service;
    }

    /**
     * Listen and validate {@link StateChangeResponse}s.
     *
     * @param response The response
     */
    @Measured
    @RabbitListener(queues = "${owms.requests.tms.to.queue-name}")
    public void onResponse(StateChangeResponse response) {

        try {
            if (!response.hasRequest()) {
                throw new ServiceLayerException(format("Got a response that is assigned to a request: [%s]", response));
            }

            if (response.hasError()) {
                var to = service.findByPKey(response.getRequest().getTransportOrderPkey());
                to.setProblem(new Message.Builder()
                        .withMessageText(response.getError().getMessage())
                        .withOccurred(response.getError().getOccurred())
                        .build());
                service.update(to);
                return;
            }

            // We only know about the states declared in TransportOrderState!
            if (TransportOrderState.STARTED.name().equals(response.getAcceptedState())) {

                // Okay the one and only voter accepted to start the TO. We could also have more than one voter but then the implementation
                // needs to be adjusted.
                this.starter.start(response.getRequest().getTransportOrderPkey());
            } else {
                throw new ServiceLayerException(format("Got a StateChangeResponse that is not supported: [%s]", response));
            }
        } catch (StateChangeException sce) {
            // fine here
        }
    }
}

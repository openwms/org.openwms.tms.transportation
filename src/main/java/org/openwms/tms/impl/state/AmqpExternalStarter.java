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

import org.ameba.annotation.Measured;
import org.ameba.exception.ServiceLayerException;
import org.openwms.core.SpringProfiles;
import org.openwms.tms.Message;
import org.openwms.tms.StateChangeException;
import org.openwms.tms.TransportOrder;
import org.openwms.tms.TransportOrderState;
import org.openwms.tms.TransportationService;
import org.openwms.tms.api.requests.state.StateChangeRequest;
import org.openwms.tms.api.requests.state.StateChangeResponse;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * A ExternalStarter.
 *
 * @author Heiko Scherrer
 */
@Profile({SpringProfiles.ASYNCHRONOUS_PROFILE})
@Lazy
@Component
class AmqpExternalStarter implements ExternalStarter {

    private final Startable starter;
    private final TransportationService<TransportOrder> service;
    private final String exchangeName;
    private final AmqpTemplate amqpTemplate;

    AmqpExternalStarter(Startable starter, TransportationService<TransportOrder> service, @Value("${owms.requests.tms.to.exchange-name}") String exchangeName, AmqpTemplate amqpTemplate) {
        this.starter = starter;
        this.service = service;
        this.exchangeName = exchangeName;
        this.amqpTemplate = amqpTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void request(String pKey) {
        StateChangeRequest req = new StateChangeRequest(pKey, TransportOrderState.STARTED.name());
        amqpTemplate.convertAndSend(exchangeName, "request.state.change", req);
    }

    /**
     * {@inheritDoc}
     */
    @Measured
    @RabbitListener(queues = "${owms.requests.tms.to.queue-name}")
    public void onResponse(StateChangeResponse response) {

        try {

            if (!response.hasRequest()) {
                throw new ServiceLayerException("Got a response that is assigned to a request: " + response);
            }

            if (response.hasError()) {
                TransportOrder to = service.findByPKey(response.getRequest().getTransportOrderPkey());
                to.setProblem(new Message.Builder()
                        .withMessage(response.getError().getMessage())
                        .withOccurred(response.getError().getOccurred())
                        .build());
                service.update(to);
                return;
            }

            // We only know about the states declared in TransportOrderState!
            if (TransportOrderState.STARTED.name().equals(response.getAcceptedState())) {

                // Okay the one and only voter accepted to start the TO. We could also have more than one voter but then the implementation
                // needs to be adjusted.
                this.starter.findAndStart(response.getRequest().getTransportOrderPkey());
            } else {
                throw new ServiceLayerException("Got a StateChangeResponse that is not supported: " + response);
            }
        } catch (StateChangeException sce) {
            // fine here
        } catch (Exception ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }
}

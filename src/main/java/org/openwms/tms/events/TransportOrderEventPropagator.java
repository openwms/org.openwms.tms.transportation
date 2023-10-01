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
package org.openwms.tms.events;

import org.openwms.core.SpringProfiles;
import org.openwms.tms.TransportOrderMapper;
import org.openwms.tms.TransportServiceEvent;
import org.openwms.tms.api.messages.TransportOrderMO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * A TransportOrderEventPropagator.
 *
 * @author Heiko Scherrer
 */
@Profile(SpringProfiles.ASYNCHRONOUS_PROFILE)
@Component
class TransportOrderEventPropagator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransportOrderEventPropagator.class);
    private final AmqpTemplate amqpTemplate;
    private final String exchangeName;
    private final TransportOrderMapper mapper;

    TransportOrderEventPropagator(AmqpTemplate amqpTemplate, @Value("${owms.events.tms.to.exchange-name}") String exchangeName, TransportOrderMapper mapper) {
        this.amqpTemplate = amqpTemplate;
        this.exchangeName = exchangeName;
        this.mapper = mapper;
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void onEvent(TransportServiceEvent event) {
        var mo = mapper.convertToMO(event.getSource());
        LOGGER.debug("Propagating event [{}]", event.getType());
        switch(event.getType()) {
            case STARTED -> {
                mo.setEventType(TransportOrderMO.EventType.STARTED);
                amqpTemplate.convertAndSend(exchangeName, "to.event.started", mo);
            }
            case TRANSPORT_FINISHED -> {
                mo.setEventType(TransportOrderMO.EventType.FINISHED);
                amqpTemplate.convertAndSend(exchangeName, "to.event.finished", mo);
            }
            case TRANSPORT_CANCELED -> {
                mo.setEventType(TransportOrderMO.EventType.CANCELED);
                amqpTemplate.convertAndSend(exchangeName, "to.event.canceled", mo);
            }
            case TRANSPORT_CREATED -> {
                mo.setEventType(TransportOrderMO.EventType.CREATED);
                amqpTemplate.convertAndSend(exchangeName, "to.event.created", mo);
            }
            default -> LOGGER.debug("Type not propagated [{}]", event.getType());
        }
    }
}

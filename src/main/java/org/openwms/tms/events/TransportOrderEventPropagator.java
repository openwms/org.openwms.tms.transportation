/*
 * Copyright 2005-2020 the original author or authors.
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

import org.ameba.mapping.BeanMapper;
import org.openwms.core.SpringProfiles;
import org.openwms.tms.TransportServiceEvent;
import org.openwms.tms.api.messages.TransportOrderMO;
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

    private final AmqpTemplate amqpTemplate;
    private final String exchangeName;
    private final BeanMapper mapper;

    TransportOrderEventPropagator(AmqpTemplate amqpTemplate, @Value("${owms.events.tms.to.exchange-name}") String exchangeName, BeanMapper mapper) {
        this.amqpTemplate = amqpTemplate;
        this.exchangeName = exchangeName;
        this.mapper = mapper;
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void onEvent(TransportServiceEvent event) {
        if (event.getType() == TransportServiceEvent.TYPE.STARTED) {
            TransportOrderMO mo = mapper.map(event.getSource(), TransportOrderMO.class);
            mo.setEventType(TransportOrderMO.EventType.STARTED);
            amqpTemplate.convertAndSend(exchangeName, "to.event.started", mo);
        }
    }
}

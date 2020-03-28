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
package org.openwms.common.transport;

import org.ameba.exception.ServiceLayerException;
import org.ameba.mapping.BeanMapper;
import org.openwms.common.transport.api.commands.TUCommand;
import org.openwms.common.transport.api.messages.TransportUnitMO;
import org.openwms.core.SpringProfiles;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.event.TransactionalEventListener;

import static java.lang.String.format;

/**
 * A TransportUnitEventPropagator.
 *
 * @author Heiko Scherrer
 */
@Profile(SpringProfiles.ASYNCHRONOUS_PROFILE)
public class TransportUnitEventPropagator {

    private final AmqpTemplate amqpTemplate;
    private final String exchangeName;
    private final BeanMapper mapper;

    public TransportUnitEventPropagator(
            AmqpTemplate amqpTemplate,
            @Value("${owms.commands.common.tu.exchange-name}") String exchangeName,
            BeanMapper mapper) {
        this.amqpTemplate = amqpTemplate;
        this.exchangeName = exchangeName;
        this.mapper = mapper;
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void onEvent(TransportUnitEvent event) {
        switch (event.getType()) {
            case CHANGE_TARGET:
                amqpTemplate.convertAndSend(exchangeName, "common.tu.command.in.change-target",
                        TUCommand.newBuilder(TUCommand.Type.CHANGE_TARGET).withTransportUnit(mapper.map(event.getSource(), TransportUnitMO.class)).build()
                );
                break;
            default:
                throw new ServiceLayerException(format("Eventtype [%s] currently not supported", event.getType()));
        }
    }
}

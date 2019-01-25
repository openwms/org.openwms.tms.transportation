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
package org.openwms.tms;

import org.ameba.annotation.Measured;
import org.ameba.annotation.TxService;
import org.ameba.exception.ServiceLayerException;
import org.ameba.mapping.BeanMapper;
import org.openwms.tms.api.TOCommand;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;

import javax.validation.Validator;

import static java.lang.String.format;

/**
 * A TransportOrderCommandHandler.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@TxService
class TransportOrderCommandHandler {

    private final TransportationService service;
    private final Validator validator;
    private final BeanMapper mapper;

    TransportOrderCommandHandler(TransportationService service, Validator validator, BeanMapper mapper) {
        this.service = service;
        this.validator = validator;
        this.mapper = mapper;
    }

    @Measured
    @RabbitListener(queues = "${owms.commands.tms.to.queue-name}")
    void receive(@Payload TOCommand command) {
        switch (command.getType()) {
            case CREATE:
                validator.validate(command.getCreateTransportOrder(), ValidationGroups.OrderCreation.class);
                service.create(command.getCreateTransportOrder().getBarcode(), command.getCreateTransportOrder().getTarget(), command.getCreateTransportOrder().getPriority());
                break;
            case CHANGE_ACTUAL_LOCATION:
            case CHANGE_TARGET:
                validator.validate(command.getUpdateTransportOrder(), ValidationGroups.OrderUpdate.class);
                service.update(mapper.map(command.getUpdateTransportOrder(), TransportOrder.class));
                break;
            default:
                throw new ServiceLayerException(format("Operation [%s] of TOCommand not supported", command.getType()));
        }
    }
}

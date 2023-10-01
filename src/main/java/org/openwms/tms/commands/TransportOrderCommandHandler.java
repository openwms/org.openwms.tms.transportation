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
package org.openwms.tms.commands;

import org.ameba.annotation.Measured;
import org.ameba.annotation.TxService;
import org.ameba.exception.ServiceLayerException;
import org.openwms.core.SpringProfiles;
import org.openwms.tms.TransportOrderMapper;
import org.openwms.tms.TransportOrderState;
import org.openwms.tms.TransportationService;
import org.openwms.tms.api.TOCommand;
import org.openwms.tms.api.ValidationGroups;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;

import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.List;

import static java.lang.String.format;

/**
 * A TransportOrderCommandHandler.
 *
 * @author Heiko Scherrer
 */
@Profile(SpringProfiles.ASYNCHRONOUS_PROFILE)
@TxService
class TransportOrderCommandHandler {

    private final TransportationService service;
    private final Validator validator;
    private final TransportOrderMapper mapper;

    TransportOrderCommandHandler(TransportationService service, Validator validator, TransportOrderMapper mapper) {
        this.service = service;
        this.validator = validator;
        this.mapper = mapper;
    }

    @Measured
    @RabbitListener(queues = "${owms.commands.tms.to.queue-name}")
    void receive(@Payload TOCommand command) {
        switch (command.getType()) {
            case CREATE -> {
                validate(command.getCreateTransportOrder(), ValidationGroups.OrderCreation.class);
                service.create(command.getCreateTransportOrder().getBarcode(), command.getCreateTransportOrder().getTarget(),
                        command.getCreateTransportOrder().getPriority());
            }
            case CHANGE_TARGET -> {
                validate(command.getUpdateTransportOrder(), ValidationGroups.OrderUpdate.class);
                service.update(mapper.convertToEO(command.getUpdateTransportOrder()));
            }
            case FINISH -> {
                validate(command.getUpdateTransportOrder(), ValidationGroups.OrderUpdate.class);
                service.change(TransportOrderState.FINISHED, List.of(command.getUpdateTransportOrder().getpKey()));
            }
            case CANCEL_ALL -> {
                var vo = command.getUpdateTransportOrder();
                var msg = mapper.convertToEO(vo.getProblem());
                service.change(vo.getBarcode(), TransportOrderState.INITIALIZED, TransportOrderState.CANCELED, msg);
                service.change(vo.getBarcode(), TransportOrderState.STARTED, TransportOrderState.CANCELED, msg);
            }
            default -> throw new ServiceLayerException(format("Operation [%s] of TOCommand not supported", command.getType()));
        }
    }

    private <T> void validate(T to, Class<?>... clazz) {
        var violations = validator.validate(to, clazz);
        if (!violations.isEmpty()) {
            var violation = violations.iterator().next();
            throw new ValidationException(String.format("Violation error [%s], property [%s]", violation.getMessage(),
                    violation.getPropertyPath()));
        }
    }
}

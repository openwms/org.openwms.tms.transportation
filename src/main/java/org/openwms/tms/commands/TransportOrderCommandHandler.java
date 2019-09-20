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
package org.openwms.tms.commands;

import org.ameba.annotation.Measured;
import org.ameba.annotation.TxService;
import org.ameba.exception.ServiceLayerException;
import org.ameba.mapping.BeanMapper;
import org.openwms.core.SpringProfiles;
import org.openwms.tms.Message;
import org.openwms.tms.TransportOrder;
import org.openwms.tms.TransportOrderState;
import org.openwms.tms.TransportationService;
import org.openwms.tms.api.TOCommand;
import org.openwms.tms.api.UpdateTransportOrderVO;
import org.openwms.tms.api.ValidationGroups;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;

import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.Set;

import static java.lang.String.format;

/**
 * A TransportOrderCommandHandler.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@Profile(SpringProfiles.ASYNCHRONOUS_PROFILE)
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
        try {
            switch (command.getType()) {
                case CREATE:
                    validate(command.getCreateTransportOrder(), ValidationGroups.OrderCreation.class);
                    service.create(command.getCreateTransportOrder().getBarcode(), command.getCreateTransportOrder().getTarget(), command.getCreateTransportOrder().getPriority());
                    break;
                case CHANGE_TARGET:
                    validate(command.getUpdateTransportOrder(), ValidationGroups.OrderUpdate.class);
                    service.update(mapper.map(command.getUpdateTransportOrder(), TransportOrder.class));
                    break;
                case FINISH:
                    validate(command.getUpdateTransportOrder(), ValidationGroups.OrderUpdate.class);
                    service.change(TransportOrderState.FINISHED, Arrays.asList(command.getUpdateTransportOrder().getpKey()));
                    break;
                case CANCEL_ALL:
                    UpdateTransportOrderVO vo = command.getUpdateTransportOrder();
                    Message msg = mapper.map(vo.getProblem(), Message.class);
                    service.change(vo.getBarcode(), TransportOrderState.INITIALIZED, TransportOrderState.CANCELED, msg);
                    service.change(vo.getBarcode(), TransportOrderState.STARTED, TransportOrderState.CANCELED, msg);
                    break;
                default:
                    throw new ServiceLayerException(format("Operation [%s] of TOCommand not supported", command.getType()));
            }
        } catch (Exception e) {
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }

    private <T> void validate(T to, Class<?>... clazz) {
        Set<ConstraintViolation<T>> violations = validator.validate(to, clazz);
        if (!violations.isEmpty()) {
            ConstraintViolation<T> violation = violations.iterator().next();
            throw new ValidationException(String.format("Violation error [%s], property [%s]", violation.getMessage(), violation.getPropertyPath()));
        }
    }
}

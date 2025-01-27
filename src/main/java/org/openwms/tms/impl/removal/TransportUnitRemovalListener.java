/*
 * Copyright 2005-2025 the original author or authors.
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
package org.openwms.tms.impl.removal;

import jakarta.validation.Validator;
import org.ameba.annotation.Measured;
import org.ameba.annotation.TxService;
import org.openwms.common.transport.api.ValidationGroups;
import org.openwms.common.transport.api.commands.TUCommand;
import org.openwms.core.SpringProfiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.Assert;

import static org.ameba.system.ValidationUtil.validate;

/**
 * A TransportUnitRemovalListener is an AMQP listener that listens on commands when a
 * TransportUnit is going to be deleted.
 *
 * @author Heiko Scherrer
 */
@Profile(SpringProfiles.ASYNCHRONOUS_PROFILE)
@TxService
class TransportUnitRemovalListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransportUnitRemovalListener.class);
    private final TransportUnitRemovalHandler handler;
    private final Validator validator;
    private final AmqpTemplate amqpTemplate;
    private final String exchangeName;

    TransportUnitRemovalListener(TransportUnitRemovalHandler handler, Validator validator, AmqpTemplate amqpTemplate, @Value("${owms.commands.common.tu.exchange-name}") String exchangeName) {
        this.handler = handler;
        this.validator = validator;
        this.amqpTemplate = amqpTemplate;
        this.exchangeName = exchangeName;
    }

    @Measured
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onEvent(TUCommand command) {
        if (command.getType() == TUCommand.Type.REMOVE) {
            validate(validator, command, ValidationGroups.TransportUnit.Remove.class);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Sending command to finally REMOVE the TransportUnit with pKey [{}]", command.getTransportUnit().getpKey());
            }
            amqpTemplate.convertAndSend(exchangeName, "common.tu.command.in.remove", command);
        }
    }

    @Measured
    @RabbitListener(queues = "${owms.commands.common.tu.queue-name}")
    public void handle(@Payload TUCommand command) {
        Assert.notNull(command, "Command is null");
        try {
            if (command.getType() == TUCommand.Type.REMOVING) {
                handler.preRemove(command);
            }
        } catch (Exception e) {
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}
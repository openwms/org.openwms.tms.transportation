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
package org.openwms.tms;

import org.ameba.annotation.Measured;
import org.openwms.core.SpringProfiles;
import org.openwms.tms.api.requests.state.StateChangeRequest;
import org.openwms.tms.api.requests.state.StateChangeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * A TestStateAcceptor.
 *
 * @author Heiko Scherrer
 */
@Profile({SpringProfiles.ASYNCHRONOUS_PROFILE})
@Component
class TestStateAcceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestStateAcceptor.class);
    private final AmqpTemplate amqpTemplate;
    private final String exchangeName = "tms.requests";

    TestStateAcceptor(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    @Measured
    @RabbitListener(queues = "test-tms-requests-queue")
    public void onRequest(StateChangeRequest request) {
        if ("STARTED".equals(request.getRequestedState())) {
            LOGGER.debug("STARTING is approved!");
            amqpTemplate.convertAndSend(exchangeName, "response.state.change", new StateChangeResponse(request, "STARTED", null));
        }
    }
}

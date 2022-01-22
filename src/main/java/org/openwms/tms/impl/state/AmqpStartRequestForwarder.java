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

import org.openwms.core.SpringProfiles;
import org.openwms.tms.TransportOrderState;
import org.openwms.tms.api.requests.state.StateChangeRequest;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * An AmqpStartRequestForwarder requests a remote service instance via an AMQP command whether a TransportOrder might be started or not.
 *
 * @author Heiko Scherrer
 */
@Profile(SpringProfiles.ASYNCHRONOUS_PROFILE)
@Component
class AmqpStartRequestForwarder implements ExternalStarter {

    private final String exchangeName;
    private final AmqpTemplate amqpTemplate;

    AmqpStartRequestForwarder(@Value("${owms.requests.tms.to.exchange-name}") String exchangeName, AmqpTemplate amqpTemplate) {
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
}

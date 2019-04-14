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
package org.openwms.tms.state;

import org.ameba.exception.NotFoundException;
import org.openwms.common.transport.api.TransportUnitApi;
import org.openwms.tms.StateChangeException;
import org.openwms.tms.TransportOrder;
import org.openwms.tms.TransportOrderState;
import org.openwms.tms.TransportServiceEvent;
import org.openwms.tms.internal.TransportOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * A Initializer.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@Transactional(propagation = Propagation.MANDATORY)
@Component
class Initializer implements ApplicationListener<TransportServiceEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);
    private final TransportOrderRepository repository;
    private final TransportUnitApi transportUnitApi;
    private final ApplicationContext ctx;

    Initializer(TransportOrderRepository repository, TransportUnitApi transportUnitApi, ApplicationContext ctx) {
        this.repository = repository;
        this.transportUnitApi = transportUnitApi;
        this.ctx = ctx;
    }

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(final TransportServiceEvent event) {
        if (event.getType() == TransportServiceEvent.TYPE.TRANSPORT_CREATED) {
            TransportOrder to = repository.findById(((TransportOrder) event.getSource()).getPk()).orElseThrow(NotFoundException::new);
            List<TransportOrder> transportOrders = repository.findByTransportUnitBKAndStates(to.getTransportUnitBK(), TransportOrderState.CREATED);
            transportOrders.sort(new TransportStartComparator());
            for (TransportOrder transportOrder : transportOrders) {
                try {
                    transportOrder
                            .changeState(TransportOrderState.INITIALIZED)
                            .setSourceLocation(transportUnitApi.findTransportUnit(transportOrder.getTransportUnitBK(), Boolean.FALSE).getActualLocation().getLocationId());
                    transportOrder = repository.save(transportOrder);
                    LOGGER.debug("TransportOrder with PK [{}] INITIALIZED", transportOrder.getPk());
                } catch (StateChangeException sce) {
                    LOGGER.warn("Could not initialize TransportOrder with PK [{}]. Message: [{}]", transportOrder.getPk(), sce.getMessage());
                    continue;
                }
                try {
                    ctx.publishEvent(new TransportServiceEvent(transportOrder, TransportServiceEvent.TYPE.INITIALIZED));
                } catch (StateChangeException sce) {
                    LOGGER.warn("Post-processing of TransportOrder with PK [{}] failed with message: [{}]", transportOrder.getPk(), sce.getMessage());
                }
            }
        }
    }
}

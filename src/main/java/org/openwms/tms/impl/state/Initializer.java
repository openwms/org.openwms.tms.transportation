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

import org.ameba.annotation.TxService;
import org.ameba.exception.NotFoundException;
import org.openwms.common.transport.api.TransportUnitApi;
import org.openwms.tms.StateChangeException;
import org.openwms.tms.StateManager;
import org.openwms.tms.TransportOrderState;
import org.openwms.tms.TransportServiceEvent;
import org.openwms.tms.impl.TransportOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.REQUIRED;

/**
 * A Initializer.
 *
 * @author Heiko Scherrer
 */
@TxService(propagation = Propagation.REQUIRED)
class Initializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);
    private final TransportOrderRepository repository;
    private final TransportUnitApi transportUnitApi;
    private final StateManager stateManager;
    private final ApplicationContext ctx;

    Initializer(TransportOrderRepository repository, TransportUnitApi transportUnitApi, StateManager stateManager, ApplicationContext ctx) {
        this.repository = repository;
        this.transportUnitApi = transportUnitApi;
        this.stateManager = stateManager;
        this.ctx = ctx;
    }

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @EventListener
    @Transactional(propagation = REQUIRED, noRollbackFor = StateChangeException.class)
    public void onEvent(final TransportServiceEvent event) {
        if (event.getType() == TransportServiceEvent.TYPE.TRANSPORT_CREATED) {
            var to = repository.findById(event.getSource().getPk()).orElseThrow(NotFoundException::new);
            var transportOrders = repository.findByTransportUnitBKAndStates(to.getTransportUnitBK(), TransportOrderState.CREATED);
            transportOrders.sort(new TransportStartComparator());
            for (var transportOrder : transportOrders) {
                try {
                    transportOrder
                            .changeState(stateManager, TransportOrderState.INITIALIZED)
                            .setSourceLocation(
                                    transportUnitApi.findTransportUnit(transportOrder.getTransportUnitBK()).getActualLocation().getLocationId()
                            );
                    transportOrder = repository.save(transportOrder);
                    LOGGER.debug("TransportOrder with pKey [{}] INITIALIZED", transportOrder.getPersistentKey());
                } catch (StateChangeException sce) {
                    LOGGER.warn("Could not initialize TransportOrder with pKey [{}]. Message: [{}]", transportOrder.getPersistentKey(), sce.getMessage());
                    continue;
                }
                try {
                    ctx.publishEvent(new TransportServiceEvent(transportOrder, TransportServiceEvent.TYPE.INITIALIZED));
                } catch (StateChangeException sce) {
                    LOGGER.warn("Post-processing of TransportOrder with pKey [{}] failed with message: [{}]", transportOrder.getPersistentKey(), sce.getMessage());
                }
            }
        }
    }
}

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

import org.ameba.annotation.Measured;
import org.ameba.annotation.TxService;
import org.ameba.i18n.Translator;
import org.openwms.tms.StateChangeException;
import org.openwms.tms.StateManager;
import org.openwms.tms.TMSMessageCodes;
import org.openwms.tms.TransportOrder;
import org.openwms.tms.TransportOrderState;
import org.openwms.tms.impl.TransportOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;

import javax.persistence.Transient;
import java.util.Date;

import static java.lang.String.format;
import static org.openwms.tms.TMSMessageCodes.INITIALIZATION_NOT_ALLOWED;
import static org.openwms.tms.TransportOrderState.CANCELED;
import static org.openwms.tms.TransportOrderState.INITIALIZED;
import static org.openwms.tms.TransportOrderState.ONFAILURE;
import static org.openwms.tms.TransportOrderState.STARTED;

/**
 * A StateManagerImpl.
 *
 * @author Heiko Scherrer
 */
@TxService(propagation = Propagation.MANDATORY)// don't because it is called within a Hibernate generation
class StateManagerImpl implements StateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateManagerImpl.class);
    @Transient
    private final Translator translator;
    @Transient
    private final TransportOrderRepository repo;

    StateManagerImpl(Translator translator, TransportOrderRepository repo) {
        this.translator = translator;
        this.repo = repo;
    }

    @Measured
    @Override
    public void validate(TransportOrderState newState, TransportOrder transportOrder) throws StateChangeException {
        var state = transportOrder.getState();
        LOGGER.debug("Request to change the state of TransportOrder with pKey [{}] from [{}] to [{}]", transportOrder.getPersistentKey(), state, newState);
        if (newState == null) {
            throw new StateChangeException(translator, TMSMessageCodes.TO_STATE_CHANGE_NULL_STATE, transportOrder.getPersistentKey());
        }
        if (state.compareTo(newState) > 0) {
            // Don't allow to turn back the state!
            throw new StateChangeException(translator, TMSMessageCodes.TO_STATE_CHANGE_BACKWARDS_NOT_ALLOWED, transportOrder.getPersistentKey());
        }
        switch (state) {
            case CREATED -> {
                if (newState != INITIALIZED && newState != CANCELED) {
                    throw new StateChangeException(
                            translator,
                            TMSMessageCodes.TO_STATE_CHANGE_NOT_READY,
                            newState,
                            transportOrder.getPersistentKey());
                }
                if (!transportOrder.hasTransportUnitBK() || !transportOrder.hasTargetLocation() && !transportOrder.hasTargetLocationGroup()) {
                    throw new StateChangeException(translator,
                            INITIALIZATION_NOT_ALLOWED,
                            transportOrder.getTransportUnitBK(),
                            transportOrder.getTargetLocation(),
                            transportOrder.getTargetLocationGroup());
                }
            }
            case INITIALIZED -> {
                if (newState != STARTED && newState != CANCELED && newState != ONFAILURE) {
                    throw new StateChangeException(
                            translator,
                            TMSMessageCodes.STATE_CHANGE_ERROR_FOR_INITIALIZED_TO,
                            transportOrder.getPersistentKey());
                }
                if (newState == STARTED && !repo.findByTransportUnitBKAndStates(transportOrder.getTransportUnitBK(), STARTED).isEmpty()) {
                    throw new StateChangeException(
                            translator,
                            TMSMessageCodes.START_TO_NOT_ALLOWED_ALREADY_STARTED_ONE,
                            transportOrder.getTransportUnitBK(), transportOrder.getPersistentKey());
                }
                LOGGER.debug("Current state is [{}], new state is [{}], # of started is [{}]", state, newState,
                        repo.numberOfTransportOrders(transportOrder.getTransportUnitBK(), STARTED));
            }
            case STARTED -> { /* All fine here. */ }
            case FINISHED, ONFAILURE, CANCELED -> throw new StateChangeException(
                    translator,
                    TMSMessageCodes.TO_STATE_CHANGE_BACKWARDS_NOT_ALLOWED,
                    transportOrder.getPersistentKey()
            );
            default -> throw new IllegalStateException(format("State not managed: [%s]", state));
        }
        switch (newState) {
            case STARTED -> transportOrder.setStartDate(new Date());
            case FINISHED, ONFAILURE, CANCELED -> transportOrder.setEndDate(new Date());
            // OK for all others
        }
        LOGGER.debug("Request processed, order is now [{}]", newState);
    }
}

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
package org.openwms.tms.impl.state;

import org.ameba.annotation.TxService;
import org.ameba.exception.NotFoundException;
import org.openwms.common.location.LocationPK;
import org.openwms.common.location.api.LocationApi;
import org.openwms.common.location.api.LocationGroupApi;
import org.openwms.common.location.api.LocationGroupVO;
import org.openwms.common.location.api.LocationVO;
import org.openwms.tms.StateChangeException;
import org.openwms.tms.TransportOrder;
import org.openwms.tms.TransportOrderState;
import org.openwms.tms.TransportServiceEvent;
import org.openwms.tms.impl.TransportOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

/**
 * A Starter.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@TxService
class Starter implements Startable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Starter.class);
    private final TransportOrderRepository repository;
    private final LocationApi locationApi;
    private final LocationGroupApi locationGroupApi;
    private final ApplicationContext ctx;
    @Autowired(required = false)
    private ExternalStarter externalStarter;

    Starter(TransportOrderRepository repository, LocationApi locationApi, LocationGroupApi locationGroupApi, ApplicationContext ctx) {
        this.repository = repository;
        this.locationApi = locationApi;
        this.locationGroupApi = locationGroupApi;
        this.ctx = ctx;
    }

    /**
     * Find and return a {@link TransportOrder} by its unique identifying persistent key.
     *
     * @param pKey The unique persistent identifying key
     * @throws NotFoundException If no instance with the given arguments exist
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = StateChangeException.class)
    public void start(String pKey) {
        this.startInternal(repository.findByPKey(pKey).orElseThrow(() -> new NotFoundException(format("No TransportOrder with pKey [%s] found", pKey))));
    }

    @Override
    public void startNext(String barcode) {
        List<TransportOrder> transportOrders = repository.findByTransportUnitBKAndStates(barcode, TransportOrderState.INITIALIZED);
        if (!transportOrders.isEmpty()) {
            triggerStart(transportOrders.get(0));
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = StateChangeException.class)
    public void triggerStart(TransportOrder to) {
        if (externalStarter == null) {
            startInternal(to);
        } else {
            externalStarter.request(to.getPersistentKey());
        }
    }

        /**
         * Call to start the {@link TransportOrder} identified by the information of {@code to}.
         *
         * @param to At last one of the targets must be present as well as the referring TransportUnitID
         * @throws NotFoundException If input parameters are not valid
         * @throws StateChangeException If it is not allowed to change the TransportOrders state
         */
    private void startInternal(TransportOrder to) {
        LOGGER.debug("> Request to start the TransportOrder with PKey [{}]", to.getPersistentKey());
        Optional<LocationGroupVO> lg = to.getTargetLocationGroup() == null ? Optional.empty() : locationGroupApi.findByName(to.getTargetLocationGroup());
        Optional<LocationVO> loc = to.getTargetLocation() != null && LocationPK.isValid(to.getTargetLocation())
                ? locationApi.findLocationByCoordinate(to.getTargetLocation())
                : Optional.empty();
        if (!lg.isPresent() && !loc.isPresent()) {
            // At least one target must be set
            throw new NotFoundException("Neither a valid target LocationGroup nor a Location are set, hence it is not possible to start the TransportOrder");
        }
        if (lg.isPresent()) {
            if (lg.get().isInfeedBlocked()) {
                throw new StateChangeException("Cannot start the TransportOrder because TargetLocationGroup is blocked");
            }
            to.setTargetLocationGroup(lg.get().asString());
        } else {
            to.setTargetLocationGroup(null);
        }
        if (loc.isPresent()) {
            if (loc.get().isInfeedBlocked()) {
                throw new StateChangeException("Cannot start the TransportOrder because TargetLocation is blocked");
            }
            to.setTargetLocation(loc.get().asString());
        } else {
            to.setTargetLocation(null);
        }

        List<TransportOrder> others = repository.findByTransportUnitBKAndStates(to.getTransportUnitBK(), TransportOrderState.STARTED);
        if (!others.isEmpty()) {
            throw new StateChangeException(format("Cannot start TransportOrder for TransportUnit [%s] because [%s] TransportOrders already started [%s]", to.getTransportUnitBK(), others.size(), others.get(0).getPersistentKey()));
        }
        to.changeState(TransportOrderState.STARTED);
        repository.save(to);
        LOGGER.info("TransportOrder for TransportUnit with Barcode [{}] STARTED at [{}]. Persisted key is [{}]", to.getTransportUnitBK(), to.getStartDate(), to.getPk());
        ctx.publishEvent(new TransportServiceEvent(to, TransportServiceEvent.TYPE.STARTED));
    }
}

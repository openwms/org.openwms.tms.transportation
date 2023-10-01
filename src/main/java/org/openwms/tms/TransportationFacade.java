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
import org.openwms.tms.api.TransportOrderApi;
import org.openwms.tms.api.TransportOrderVO;
import org.openwms.tms.api.UpdateTransportOrderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * A TransportationFacade is a transactional Spring managed bean that is independent from the used API exporter pattern.
 *
 * @author Heiko Scherrer
 */
@Transactional
@Component
public class TransportationFacade implements TransportOrderApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransportationFacade.class);
    private final TransportOrderMapper mapper;
    private final TransportationService<TransportOrder> service;

    TransportationFacade(TransportOrderMapper mapper, TransportationService<TransportOrder> service) {
        this.mapper = mapper;
        this.service = service;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Measured
    public void createTO(String barcode, String target) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create TransportOrder with Barcode [{}] and Target [{}]", barcode, target);
        }
        service.create(barcode, target, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Measured
    public void createTO(String barcode, String target, String priority) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create TransportOrder with Barcode [{}] and Target [{}] and Priority [{}]", barcode, target, priority);
        }
        service.create(barcode, target, priority);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Measured
    public void changeState(String pKey, String state) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Change the state of the TransportOrder with persistent key [{}] to [{}]", pKey, state);
        }
        var order = service.findByPKey(pKey);
        var failures = service.change(
                order.getTransportUnitBK(),
                order.getState(),
                TransportOrderState.valueOf(state),
                null
        );
        if (!failures.isEmpty()) {
            LOGGER.error("Failed to changed TransportOrders [{}]", failures.toArray());
            var message = failures.iterator().next();
            throw new StateChangeException(message.getMessageText(), message.getMessageNo(), message.getpKey());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Measured
    public void updateTO(String pKey, UpdateTransportOrderVO vo) {
        if (vo.getPriority() != null && !vo.getPriority().isEmpty()) {
            PriorityLevel.of(vo.getPriority());
        }
        if (vo.getpKey() == null || !pKey.equals(vo.getpKey())) {
            vo.setpKey(pKey);
        }
        service.update(mapper.convertToEO(vo));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Measured
    public List<TransportOrderVO> findBy(String barcode, String state) {
        var orders = mapper.convertToVO(service.findBy(barcode, state));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Found [{}] TransportOrders with barcode [{}] in state [{}]", orders.size(), barcode, state);
        }
        return orders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Measured
    public TransportOrderVO findByPKey(String pKey) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Find TransportOrder with persistent key [{}]", pKey);
        }
        return mapper.convertToVO(service.findByPKey(pKey));
    }
}

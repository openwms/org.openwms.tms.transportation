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
package org.openwms.tms.impl;

import org.ameba.annotation.TxService;
import org.ameba.mapping.BeanMapper;
import org.openwms.tms.PriorityLevel;
import org.openwms.tms.TransportOrder;
import org.openwms.tms.TransportOrderState;
import org.openwms.tms.TransportationService;
import org.openwms.tms.api.TransportOrderApi;
import org.openwms.tms.api.TransportOrderVO;
import org.openwms.tms.api.UpdateTransportOrderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A TransportationFacade is a transactional Spring managed bean that is independent from
 * the used API exporter pattern.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@TxService
class TransportationFacade implements TransportOrderApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransportationFacade.class);
    private final BeanMapper mapper;
    private final TransportationService<TransportOrder> service;

    TransportationFacade(BeanMapper mapper, TransportationService<TransportOrder> service) {
        this.mapper = mapper;
        this.service = service;
    }

    @Override
    public void createTO(String barcode, String target) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create TransportOrder with Barcode [{}] and Target [{}]", barcode, target);
        }
        service.create(barcode, target, null);
    }

    @Override
    public void createTO(String barcode, String target, String priority) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create TransportOrder with Barcode [{}] and Target [{}] and Priority [{}]", barcode, target, priority);
        }
        service.create(barcode, target, priority);
    }

    @Override
    public void changeState(String pKey, String state) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Change the state of the TransportOrder with persistent key [{}] to [{}]", pKey, state);
        }
        TransportOrder order = service.findByPKey(pKey);
        service.change(order.getTransportUnitBK(), order.getState(), TransportOrderState.valueOf(state), null);
    }

    @Override
    public void updateTO(String pKey, UpdateTransportOrderVO vo) {
        if (vo.getPriority() != null && !vo.getPriority().isEmpty()) {
            PriorityLevel.of(vo.getPriority());
        }
        if (vo.getpKey() == null || !pKey.equals(vo.getpKey())) {
            vo.setpKey(pKey);
        }
        service.update(mapper.map(vo, TransportOrder.class));
    }

    @Override
    public List<TransportOrderVO> findBy(String barcode, String state) {
        List<TransportOrderVO> orders = mapper.map(service.findBy(barcode, state), TransportOrderVO.class);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Found [{}] TransportOrders with barcode [{}] in state [{}]", orders.size(), barcode, state);
        }
        return orders;
    }

    @Override
    public TransportOrderVO findByPKey(String pKey) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Find TransportOrder with persistent key [{}]", pKey);
        }
        return mapper.map(service.findByPKey(pKey), TransportOrderVO.class);
    }
}

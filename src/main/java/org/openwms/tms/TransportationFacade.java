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
package org.openwms.tms;

import org.ameba.annotation.TxService;
import org.ameba.mapping.BeanMapper;
import org.openwms.tms.api.TransportOrderApi;
import org.openwms.tms.api.TransportOrderVO;
import org.openwms.tms.api.UpdateTransportOrderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * A TransportationFacade is a transactional Spring managed bean that is independent from
 * the used API exporter pattern.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@TxService
public class TransportationFacade implements TransportOrderApi {

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
        service.change(TransportOrderState.valueOf(state), Collections.singleton(pKey));
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

    @Override
    public TransportOrderVO getNextInfeed(String sourceLocation, String state, String searchTargetLocationGroups) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Find TransportOrder from infeed position [{}] in state [{}]", sourceLocation, state);
        }
        List<TransportOrder> tos = service.findInfeed(TransportOrderState.valueOf(state), sourceLocation, searchTargetLocationGroups);
        if (tos.isEmpty()) {
            LOGGER.debug("> No TransportOrder for infeed exists");
            return null;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("> Found TransportOrder with pk [{}] for infeed", tos.get(0).getPk());
        }
        return mapper.map(tos.get(0), TransportOrderVO.class);
    }

    @Override
    public TransportOrderVO getNextInAisle(String sourceLocationGroupName, String targetLocationGroupName, String state) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Find TransportOrders within one aisle with source [{}] and target [{}] in state [{}]", sourceLocationGroupName, targetLocationGroupName, state);
        }
        List<TransportOrder> tos = service.findInAisle(TransportOrderState.valueOf(state), sourceLocationGroupName, targetLocationGroupName);
        if (tos.isEmpty()) {
            LOGGER.debug("> No in-aisle TransportOrders exist");
            return null;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("> [{}] in-aisle TransportOrders exists, returning the first one with pk [{}]", tos.size(), tos.get(0).getPk());
        }
        return mapper.map(tos.get(0), TransportOrderVO.class);
    }

    @Override
    public TransportOrderVO getNextOutfeed(String state, String sourceLocationGroupName) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Find TransportOrders for outfeed position [{}] in state [{}]", sourceLocationGroupName, state);
        }
        List<TransportOrder> tos = service.findOutfeed(TransportOrderState.valueOf(state), sourceLocationGroupName);
        if (tos.isEmpty()) {
            LOGGER.debug("> No TransportOrder for outfeed exists");
            return null;
        }
        TransportOrder to = tos.get(0);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("> TransportOrder with pk [{}] exists for outfeed", to.getPk());
        }
        return mapper.map(to, TransportOrderVO.class);
    }
}

/*
 * openwms.org, the Open Warehouse Management System.
 * Copyright (C) 2014 Heiko Scherrer
 *
 * This file is part of openwms.org.
 *
 * openwms.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * openwms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.openwms.tms.api;

import org.ameba.annotation.Measured;
import org.ameba.exception.BehaviorAwareException;
import org.ameba.exception.BusinessRuntimeException;
import org.ameba.http.Response;
import org.ameba.mapping.BeanMapper;
import org.openwms.tms.PriorityLevel;
import org.openwms.tms.TMSConstants;
import org.openwms.tms.TransportOrder;
import org.openwms.tms.TransportOrderState;
import org.openwms.tms.TransportationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A TransportationController.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@RestController(TMSConstants.ROOT_ENTITIES)
class TransportationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransportationController.class);
    private final BeanMapper m;
    private final TransportationService<TransportOrder> service;

    TransportationController(BeanMapper m, TransportationService<TransportOrder> service) {
        this.m = m;
        this.service = service;
    }

    @Measured
    @GetMapping(value = TMSConstants.ROOT_ENTITIES, params = {"barcode", "state"})
    List<TransportOrder> findBy(@RequestParam String barcode, @RequestParam String state) {
        return new ArrayList<>(service.findBy(barcode, state));
    }

    @Measured
    @GetMapping(TMSConstants.ROOT_ENTITIES + "/{pKey}")
    TransportOrder findByPKey(@PathVariable String pKey) {
        LOGGER.debug("Find TransportOrder with persistent key {}", pKey);
        return service.findByPKey(pKey);
    }

    @Measured
    @GetMapping(value = TMSConstants.ROOT_ENTITIES, params ={"sourceLocation", "state", "searchTargetLocationGroupNames"})
    TransportOrder getNextInfeed(@RequestParam("sourceLocation") String sourceLocation, @RequestParam("state") String state, @RequestParam("searchTargetLocationGroupNames") String searchTargetLocationGroups) {
        LOGGER.debug("Find TransportOrders from infeed position {} in state {}", sourceLocation, state);
        List<TransportOrder> tos = service.findInfeed(sourceLocation, TransportOrderState.valueOf(state), searchTargetLocationGroups);
        if (tos.isEmpty()) {
            LOGGER.debug("> No TransportOrder for infeed exists");
            return null;
        }
        LOGGER.debug("> TransportOrder with id {} exists for infeed", tos.get(0).getPk());
        return tos.get(0);
    }

    @Measured
    @GetMapping(value = "/transportorders", params ={"sourceLocationGroupName", "targetLocationGroupName", "state"})
    TransportOrder getNextInAisle(@RequestParam("sourceLocationGroupName") String sourceLocationGroupName, @RequestParam("targetLocationGroupName") String targetLocationGroupName, @RequestParam("state") String state) {
        LOGGER.debug("Find TransportOrders within one aisle with source {} and target {} in state {}", sourceLocationGroupName, targetLocationGroupName, state);
        Collection<TransportOrder> tos = service.findInAisle(sourceLocationGroupName, targetLocationGroupName, TransportOrderState.valueOf(state));
        if (tos.isEmpty()) {
            LOGGER.debug("> No TransportOrders exist");
            return null;
        }
        LOGGER.debug("> {} TransportOrders exist", tos.size());
        return tos.iterator().next();
    }

    @GetMapping(value = "/transportorders", params ={"sourceLocationGroupName", "state"})
    TransportOrder getNextOutfeed(@RequestParam("sourceLocationGroupName") String sourceLocationGroupName,@RequestParam("state") String state) {
        LOGGER.debug("Find TransportOrders for outfeed position {} in state {}", sourceLocationGroupName, state);
        List<TransportOrder> tos = service.findOutfeed(sourceLocationGroupName, TransportOrderState.valueOf(state));
        if (tos.isEmpty()) {
            LOGGER.debug("> No TransportOrder for outfeed exists");
            return null;
        }
        TransportOrder to = tos.get(0);
        LOGGER.debug("> TransportOrder with id {} exists for outfeed", to.getPk());
        return to;
    }


    @Measured
    @PostMapping(params = {"barcode", "target"})
    @ResponseStatus(HttpStatus.CREATED)
    void createTO(@RequestParam(value = "barcode") String barcode, @RequestParam(value = "target") String target, @RequestParam(value = "priority", required = false) String priority, HttpServletRequest req, HttpServletResponse resp) {
        TransportOrder to = service.create(barcode, target, priority);
        resp.addHeader(HttpHeaders.LOCATION, getCreatedResourceURI(req, to.getPersistentKey()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void createTO(@RequestBody CreateTransportOrderVO vo, HttpServletRequest req, HttpServletResponse resp) {
        TransportOrder to = service.create(vo.getBarcode(), vo.getTarget(), vo.getPriority());
        resp.addHeader(HttpHeaders.LOCATION, getCreatedResourceURI(req, to.getPersistentKey()));
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updateTO(@RequestBody UpdateTransportOrderVO vo) {
        PriorityLevel.of(vo.getPriority());
        service.update(m.map(vo, TransportOrder.class));
    }

    @PostMapping(value = "/transportorders/{id}", params = {"state"})
    void finishTO(@PathVariable(value = "id") String id, @RequestParam(value = "state") String state) {
        service.changeState(id, TransportOrderState.valueOf(state));
    }


    @ExceptionHandler(BusinessRuntimeException.class)
    public ResponseEntity<Response<Serializable>> handleNotFound(HttpServletResponse res, BusinessRuntimeException ex) {
        if (ex instanceof BehaviorAwareException) {
            BehaviorAwareException bae = (BehaviorAwareException) ex;
            return new ResponseEntity<>(new Response<>(ex.getMessage(), bae.getMsgKey(), bae.getStatus().toString(), bae.getData()), bae.getStatus());
        }
        return new ResponseEntity<>(new Response<>(ex.getMessage(), ex.getMsgKey(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), new String[]{ex.getMsgKey()}), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<Serializable>> handleBadRequests(HttpServletResponse res, IllegalArgumentException ex) {
        return new ResponseEntity<>(new Response<>(ex.getMessage(), HttpStatus.BAD_REQUEST.toString()), HttpStatus.BAD_REQUEST);
    }

    private String getCreatedResourceURI(HttpServletRequest req, String objId) {
        StringBuffer url = req.getRequestURL();
        UriTemplate template = new UriTemplate(url.append("/{objId}/").toString());
        return template.expand(objId).toASCIIString();
    }
}

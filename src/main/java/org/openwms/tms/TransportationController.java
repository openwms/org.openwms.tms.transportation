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

import org.ameba.annotation.Measured;
import org.ameba.exception.BehaviorAwareException;
import org.ameba.exception.BusinessRuntimeException;
import org.ameba.http.Response;
import org.ameba.mapping.BeanMapper;
import org.openwms.tms.api.CreateTransportOrderVO;
import org.openwms.tms.api.UpdateTransportOrderVO;
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
import java.util.ArrayList;
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
        List<TransportOrder> tos = service.findInfeed(TransportOrderState.valueOf(state), sourceLocation, searchTargetLocationGroups);
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
        List<TransportOrder> tos = service.findInAisle(TransportOrderState.valueOf(state), sourceLocationGroupName, targetLocationGroupName);
        if (tos.isEmpty()) {
            LOGGER.debug("> No TransportOrders exist");
            return null;
        }
        LOGGER.debug("> {} TransportOrders exist", tos.size());
        return tos.get(0);
    }

    @Measured
    @GetMapping(value = "/transportorders", params = {"state", "sourceLocationGroupName"})
    TransportOrder getNextOutfeed(@RequestParam("state") String state, @RequestParam("sourceLocationGroupName") String sourceLocationGroupName) {
        LOGGER.debug("Find TransportOrders for outfeed position {} in state {}", sourceLocationGroupName, state);
        List<TransportOrder> tos = service.findOutfeed(TransportOrderState.valueOf(state), sourceLocationGroupName);
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

    @Measured
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void createTO(@RequestBody CreateTransportOrderVO vo, HttpServletRequest req, HttpServletResponse resp) {
        TransportOrder to = service.create(vo.getBarcode(), vo.getTarget(), vo.getPriority());
        resp.addHeader(HttpHeaders.LOCATION, getCreatedResourceURI(req, to.getPersistentKey()));
    }

    @Measured
    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updateTO(@RequestBody UpdateTransportOrderVO vo) {
        PriorityLevel.of(vo.getPriority());
        service.update(m.map(vo, TransportOrder.class));
    }

    @Measured
    @PostMapping(value = "/transportorders/{id}", params = {"state"})
    void finishTO(@PathVariable(value = "id") String pKey, @RequestParam(value = "state") String state) {
        service.changeState(TransportOrderState.valueOf(state), pKey);
    }


    @ExceptionHandler(BusinessRuntimeException.class)
    public ResponseEntity<Response> handleNotFound(HttpServletResponse res, BusinessRuntimeException ex) {
        if (ex instanceof BehaviorAwareException) {
            BehaviorAwareException bae = (BehaviorAwareException) ex;
            return new ResponseEntity<>(
                    Response.newBuilder()
                            .withMessage(ex.getMessage())
                            .withMessageKey(bae.getMessageKey())
                            .withHttpStatus(bae.getStatus().toString())
                            .withObj(bae.getData())
                            .build(),
                    bae.getStatus());
        }
        return new ResponseEntity<>(
                Response.newBuilder()
                        .withMessage(ex.getMessage())
                        .withMessageKey(ex.getMessageKey())
                        .withHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                        .withObj(new String[]{ex.getMessageKey()})
                        .build(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response> handleBadRequests(HttpServletResponse res, IllegalArgumentException ex) {
        return new ResponseEntity<>(
                Response.newBuilder()
                        .withMessage(ex.getMessage())
                        .withHttpStatus(HttpStatus.BAD_REQUEST.toString())
                        .build(),
                HttpStatus.BAD_REQUEST);
    }

    private String getCreatedResourceURI(HttpServletRequest req, String objId) {
        StringBuffer url = req.getRequestURL();
        UriTemplate template = new UriTemplate(url.append("/{objId}/").toString());
        return template.expand(objId).toASCIIString();
    }
}

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
import org.openwms.tms.api.CreateTransportOrderVO;
import org.openwms.tms.api.TMSApi;
import org.openwms.tms.api.TransportOrderVO;
import org.openwms.tms.api.UpdateTransportOrderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
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
import java.util.List;

/**
 * A TransportationController.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@Profile("!INMEM")
@RestController
class TransportationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransportationController.class);
    private final TransportationService<TransportOrder> service;
    private final TransportationFacade transportationFacade;

    TransportationController(TransportationService<TransportOrder> service, TransportationFacade transportationFacade) {
        this.service = service;
        this.transportationFacade = transportationFacade;
    }

    @Measured
    @GetMapping(value = TMSApi.TRANSPORT_ORDERS, params = {"barcode", "state"})
    public List<TransportOrderVO> findBy(@RequestParam String barcode, @RequestParam String state) {
        return transportationFacade.findBy(barcode, state);
    }

    @Measured
    @GetMapping(TMSApi.TRANSPORT_ORDERS + "/{pKey}")
    public TransportOrderVO findByPKey(@PathVariable(value = "pKey") String pKey) {
        return transportationFacade.findByPKey(pKey);
    }

    @Measured
    @PostMapping(value = TMSApi.TRANSPORT_ORDERS, params = {"barcode", "target"})
    @ResponseStatus(HttpStatus.CREATED)
    public void createTO(@RequestParam(value = "barcode") String barcode, @RequestParam(value = "target") String target, @RequestParam(value = "priority", required = false) String priority, HttpServletRequest req, HttpServletResponse resp) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create TransportOrder with Barcode [{}] and Target [{}] and Priority [{}]", barcode, target, priority);
        }
        TransportOrder to = service.create(barcode, target, priority);
        resp.addHeader(HttpHeaders.LOCATION, getCreatedResourceURI(req, to.getPersistentKey()));
    }

    @Measured
    @PostMapping(TMSApi.TRANSPORT_ORDERS)
    @ResponseStatus(HttpStatus.CREATED)
    public void createTO(@RequestBody CreateTransportOrderVO vo, HttpServletRequest req, HttpServletResponse resp) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create TransportOrder [{}]", vo.toString());
        }
        TransportOrder to = service.create(vo.getBarcode(), vo.getTarget(), vo.getPriority());
        resp.addHeader(HttpHeaders.LOCATION, getCreatedResourceURI(req, to.getPersistentKey()));
    }

    @Measured
    @PatchMapping(TMSApi.TRANSPORT_ORDERS + "/{pKey}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTO(@PathVariable(value = "pKey") String pKey, @RequestBody UpdateTransportOrderVO vo) {
        transportationFacade.updateTO(pKey, vo);
    }

    @Measured
    @PostMapping(value = TMSApi.TRANSPORT_ORDERS + "/{pKey}", params = {"state"})
    public void changeState(@PathVariable(value = "pKey") String pKey, @RequestParam(value = "state") String state) {
        transportationFacade.changeState(pKey, state);
    }

    @ExceptionHandler(BusinessRuntimeException.class)
    public ResponseEntity<Response> handleNotFound(BusinessRuntimeException ex) {
        if (ex instanceof BehaviorAwareException) {
            BehaviorAwareException bae = (BehaviorAwareException) ex;
            return new ResponseEntity<>(Response.newBuilder().withMessage(ex.getMessage()).withMessageKey(bae.getMessageKey()).withHttpStatus(bae.getStatus().toString()).withObj(bae.getData()).build(), bae.getStatus());
        }
        return new ResponseEntity<>(Response.newBuilder().withMessage(ex.getMessage()).withMessageKey(ex.getMessageKey()).withHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.toString()).withObj(new String[]{ex.getMessageKey()}).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response> handleBadRequests(IllegalArgumentException ex) {
        return new ResponseEntity<>(Response.newBuilder().withMessage(ex.getMessage()).withHttpStatus(HttpStatus.BAD_REQUEST.toString()).build(), HttpStatus.BAD_REQUEST);
    }

    private String getCreatedResourceURI(HttpServletRequest req, String objId) {
        StringBuffer url = req.getRequestURL();
        UriTemplate template = new UriTemplate(url.append("/{objId}/").toString());
        return template.expand(objId).toASCIIString();
    }
}

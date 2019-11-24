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
import org.ameba.http.Response;
import org.openwms.core.http.AbstractWebController;
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
class TransportationController extends AbstractWebController {

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
        PriorityLevel.of(priority); // validate early here!
        TransportOrder to = service.create(barcode, target, priority);
        resp.addHeader(HttpHeaders.LOCATION, getCreatedResourceURI(req, to.getPersistentKey()));
    }

    @Measured
    @PostMapping(TMSApi.TRANSPORT_ORDERS)
    @ResponseStatus(HttpStatus.CREATED)
    public void createTO(@RequestBody CreateTransportOrderVO vo, HttpServletRequest req, HttpServletResponse resp) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create TransportOrder [{}]", vo);
        }
        PriorityLevel.of(vo.getPriority()); // validate early here!
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
    public ResponseEntity<Void> changeState(@PathVariable(value = "pKey") String pKey, @RequestParam(value = "state") String state) {
        try {
            transportationFacade.changeState(pKey, state);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @ExceptionHandler(DeniedException.class)
    protected ResponseEntity<Response> handleDeniedException(DeniedException bae) {
        return new ResponseEntity<>(Response.newBuilder()
                .withMessage(bae.getMessage())
                .withMessageKey(bae.getMessageKey())
                .withHttpStatus(String.valueOf(HttpStatus.CONFLICT.value()))
                .withObj(bae.getData())
                .build(),
                HttpStatus.CONFLICT
        );
    }

    private String getCreatedResourceURI(HttpServletRequest req, String objId) {
        StringBuffer url = req.getRequestURL();
        UriTemplate template = new UriTemplate(url.append("/{objId}/").toString());
        return template.expand(objId).toASCIIString();
    }
}

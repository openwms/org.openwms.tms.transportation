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
package org.openwms.tms.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * A TransportOrderApi is a part of the Transportation Service' public RESTful API.
 *
 * @author Heiko Scherrer
 */
@FeignClient(name = "tms-service", qualifier = "transportOrderApi", decode404 = true)
public interface TransportOrderApi {

    /**
     * Find and return all {@code TransportOrder}s in a particular {@code state} for the
     * {@code TransportUnit} with the given {@code barcode}.
     *
     * @param barcode The business identifier of the TransportUnit
     * @param state The TransportOrder state
     * @return A List implementation of the result instances, never {@literal null}
     */
    @GetMapping(value = TMSApi.TRANSPORT_ORDERS, params = {"barcode", "state"})
    List<TransportOrderVO> findBy(
            @RequestParam(value = "barcode") String barcode,
            @RequestParam(value = "state") String state
    );

    /**
     * Find and return a {@code TransportOrder} identified by its persistent key.
     *
     * @param pKey The persistent key of the TransportOrder (not the primary key)
     * @return The instance
     * @throws org.ameba.exception.NotFoundException if no TransportOrder with that id exists
     */
    @GetMapping(TMSApi.TRANSPORT_ORDERS + "/{pKey}")
    TransportOrderVO findByPKey(
            @PathVariable(value = "pKey") String pKey
    );

    /**
     * Create a {@code TransportOrder} for a {@code TransportUnit} identified by the given
     * {@code barcode} to the given {@code target}.
     *
     * @param barcode The business identifier of the TransportUnit
     * @param target Either a Location of a LocationGroup
     */
    @PostMapping(value = TMSApi.TRANSPORT_ORDERS, params = {"barcode", "target"})
    @ResponseStatus(HttpStatus.CREATED)
    void createTO(
            @RequestParam(value = "barcode") String barcode,
            @RequestParam(value = "target") String target
    );

    /**
     * Create a {@code TransportOrder} for a {@code TransportUnit} identified by the given
     * {@code barcode} to the given {@code target} and the given {@code priority}.
     *
     * @param barcode The business identifier of the TransportUnit
     * @param target Either a Location of a LocationGroup
     * @param priority The priority of the TransportOrder
     */
    @PostMapping(value = TMSApi.TRANSPORT_ORDERS, params = {"barcode", "target"})
    @ResponseStatus(HttpStatus.CREATED)
    void createTO(
            @RequestParam(value = "barcode") String barcode,
            @RequestParam(value = "target") String target,
            @RequestParam(value = "priority", required = false) String priority
    );

    /**
     * Request to change the state of an existing {@code TransportOrder}.
     *
     * @param pKey The persistent key of the TransportOrder (not the primary key)
     * @param state The requested TransportOrder state
     */
    @PostMapping(value = TMSApi.TRANSPORT_ORDERS + "/{pKey}", params = {"state"})
    void changeState(
            @PathVariable(value = "pKey") String pKey,
            @RequestParam(value = "state") String state
    );

    /**
     * Request to update an existing {@code TransportOrder}.
     *
     * @param pKey The persistent key of the TransportOrder (not the primary key)
     * @param transportOrder The minimal necessary structure of the change set
     */
    @PatchMapping(TMSApi.TRANSPORT_ORDERS + "/{pKey}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updateTO(
            @PathVariable(value = "pKey") String pKey,
            @RequestBody UpdateTransportOrderVO transportOrder
    );
}

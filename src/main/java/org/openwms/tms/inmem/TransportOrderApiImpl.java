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
package org.openwms.tms.inmem;

import org.ameba.annotation.Measured;
import org.ameba.annotation.TxService;
import org.openwms.tms.TransportationFacade;
import org.openwms.tms.api.TransportOrderApi;
import org.openwms.tms.api.TransportOrderVO;
import org.openwms.tms.api.UpdateTransportOrderVO;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * A TransportOrderApiImpl.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@Profile("INMEM")
@Primary
@TxService
class TransportOrderApiImpl implements TransportOrderApi {

    private final TransportationFacade facade;

    TransportOrderApiImpl(TransportationFacade facade) {
        this.facade = facade;
    }

    @Measured
    @Override
    public void createTO(String barcode, String target) {
        facade.createTO(barcode, target, null);
    }

    @Measured
    @Override
    public void createTO(String barcode, String target, String priority) {
        facade.createTO(barcode, target, priority);
    }

    @Measured
    @Override
    public void changeState(String id, String state) {
        facade.changeState(id, state);
    }

    @Measured
    @Override
    public void updateTO(String pKey, UpdateTransportOrderVO vo) {
        facade.updateTO(pKey, vo);
    }

    @Measured
    @Override
    public List<TransportOrderVO> findBy(String barcode, String state) {
        return facade.findBy(barcode, state);
    }

    @Measured
    @Override
    public TransportOrderVO findByPKey(String pKey) {
        return facade.findByPKey(pKey);
    }
}

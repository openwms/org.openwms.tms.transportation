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
package org.openwms.tms.impl;

import org.openwms.common.transport.api.TransportUnitApi;
import org.openwms.common.transport.api.TransportUnitVO;
import org.openwms.tms.TransportOrder;
import org.openwms.tms.api.ValidationGroups;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.Set;

/**
 * A ChangeTU is responsible to change a {@link TransportOrder}s assigned {@code TransportUnit}.
 *
 * @author Heiko Scherrer
 */
@Transactional(propagation = Propagation.MANDATORY)
@Component
class ChangeTU implements UpdateFunction {

    private final TransportUnitApi transportUnitApi;
    private final Validator validator;

    @Autowired
    public ChangeTU(Validator validator, TransportUnitApi transportUnitApi) {
        this.validator = validator;
        this.transportUnitApi = transportUnitApi;
    }

    /**
     * {@inheritDoc}
     * <p>
     * If the assigned {@code TransportUnitBK} has changed, we're going to re-assign the {code TransportUnit}s.
     */
    @Override
    public void update(TransportOrder saved, TransportOrder toUpdate) {
        validateAttributes(toUpdate);
        if (toUpdate.getTransportUnitBK() != null && !saved.getTransportUnitBK().equals(toUpdate.getTransportUnitBK())) {

            // change the target of the TU to assign
            TransportUnitVO savedTU = TransportUnitVO.builder()
                    .barcode(toUpdate.getTransportUnitBK())
//                    .target(toUpdate.getTargetLocationGroup())
                    .build();
            transportUnitApi.updateTU(savedTU.getBarcode(), savedTU);

            // clear target of an formerly assigned TU
            savedTU = TransportUnitVO.builder()
                    .barcode(saved.getTransportUnitBK())
//                    .target("")
                    .build();
            transportUnitApi.updateTU(savedTU.getBarcode(), savedTU);

            saved.setTransportUnitBK(toUpdate.getTransportUnitBK());
        }
    }

    private void validateAttributes(TransportOrder to) {
        Set<ConstraintViolation<TransportOrder>> violations = validator.validate(to, ValidationGroups.class);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations.iterator().next().getMessage());
        }
    }
}

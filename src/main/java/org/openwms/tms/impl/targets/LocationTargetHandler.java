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
package org.openwms.tms.impl.targets;

import org.ameba.annotation.Measured;
import org.openwms.common.location.api.LocationVO;
import org.openwms.tms.impl.TargetHandler;
import org.openwms.tms.impl.TransportOrderRepository;
import org.springframework.stereotype.Component;

/**
 * A LocationTargetHandler.
 *
 * @author Heiko Scherrer
 */
@Component
class LocationTargetHandler implements TargetHandler<LocationVO> {

    private final TransportOrderRepository repository;

    LocationTargetHandler(TransportOrderRepository repository) {
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Measured
    public int getNoTOToTarget(LocationVO target) {
        var result = repository.findByTargetLocation(target.asString());
        return result != null ? result.size() : 0;
    }
}

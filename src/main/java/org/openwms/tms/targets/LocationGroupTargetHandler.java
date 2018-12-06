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
package org.openwms.tms.targets;

import org.openwms.common.location.api.LocationGroupVO;
import org.openwms.tms.TargetHandler;
import org.openwms.tms.TransportOrder;
import org.openwms.tms.TransportOrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * A LocationGroupTargetHandler.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@Component
class LocationGroupTargetHandler implements TargetHandler<LocationGroupVO> {

    private final TransportOrderRepository repository;

    public LocationGroupTargetHandler(TransportOrderRepository repository) {
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNoTOToTarget(LocationGroupVO target) {
        List<TransportOrder> result = repository.findByTargetLocation(target.asString());
        return result != null ? result.size() : 0;
    }
}

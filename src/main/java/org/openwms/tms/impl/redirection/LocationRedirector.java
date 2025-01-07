/*
 * Copyright 2005-2025 the original author or authors.
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
package org.openwms.tms.impl.redirection;

import org.openwms.common.location.api.LocationApi;
import org.openwms.common.location.api.LocationVO;
import org.openwms.common.transport.TransportUnitEvent;
import org.openwms.common.transport.api.TransportUnitVO;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * A LocationRedirector votes for a {@link RedirectVote} whether the target location is enabled for infeed. The class is lazy initialized.
 *
 * @author Heiko Scherrer
 */
@Lazy
@Order(5)
@Component
class LocationRedirector extends TargetRedirector<LocationVO> {

    private final LocationApi locationApi;
    private final ApplicationContext ctx;

    LocationRedirector(LocationApi locationApi, ApplicationContext ctx) {
        this.locationApi = locationApi;
        this.ctx = ctx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isTargetAvailable(LocationVO target) {
        return target.getIncomingActive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Optional<LocationVO> resolveTarget(RedirectVote vote) {
        return (vote.getTargetLocation() == null || vote.getTargetLocation().isEmpty())
                ? Optional.empty()
                : locationApi.findById(vote.getTargetLocation()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void assignTarget(RedirectVote vote) {
        vote.getTransportOrder().setTargetLocation(vote.getTargetLocation());
        ctx.publishEvent(
                TransportUnitEvent.newBuilder()
                        .tu(TransportUnitVO.builder()
                                .barcode(vote.getTransportOrder().getTransportUnitBK())
                                .build())
                        .type(TransportUnitEvent.TransportUnitEventType.CHANGE_TARGET)
                        .build()
        );
    }
}
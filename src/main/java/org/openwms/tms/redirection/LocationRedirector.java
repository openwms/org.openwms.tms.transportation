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
package org.openwms.tms.redirection;

import org.openwms.common.location.api.LocationApi;
import org.openwms.common.location.api.LocationVO;
import org.openwms.common.transport.api.TransportUnitVO;
import org.openwms.common.transport.events.TransportUnitEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * A LocationRedirector votes for a {@link RedirectVote} whether the target location is enabled for infeed. The class is lazy initialized.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@Lazy
@Order(5)
@Component
class LocationRedirector extends TargetRedirector<LocationVO> {

    private final LocationApi locationApi;
    private final ApplicationContext ctx;

    @Autowired
    public LocationRedirector(LocationApi locationApi, ApplicationContext ctx) {
        this.locationApi = locationApi;
        this.ctx = ctx;
    }

    @Override
    protected boolean isTargetAvailable(LocationVO target) {
        return target.isIncomingActive();
    }

    @Override
    protected Optional<LocationVO> resolveTarget(RedirectVote vote) {
        return locationApi.findLocationByCoordinate(vote.getTarget());
    }

    @Override
    protected void assignTarget(RedirectVote vote) {
        vote.getTransportOrder().setTargetLocation(vote.getTarget());
        TransportUnitVO transportUnit = new TransportUnitVO();
        transportUnit.setBarcode(vote.getTransportOrder().getTransportUnitBK());
        transportUnit.setTarget(vote.getTarget());
        ctx.publishEvent(TransportUnitEvent.newBuilder().tu(transportUnit).type(TransportUnitEvent.TransportUnitEventType.CHANGE_TARGET).build());
    }
}
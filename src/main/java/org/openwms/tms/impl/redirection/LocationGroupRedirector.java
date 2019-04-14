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
package org.openwms.tms.impl.redirection;

import org.openwms.common.location.api.LocationGroupApi;
import org.openwms.common.location.api.LocationGroupVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * A LocationGroupRedirector votes for a {@link RedirectVote} whether the target locationGroup is enabled for infeed. The class is lazy
 * initialized.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@Lazy
@Order(10)
@Component
class LocationGroupRedirector extends TargetRedirector<LocationGroupVO> {

    private final LocationGroupApi locationGroupApi;

    @Autowired
    public LocationGroupRedirector(LocationGroupApi locationGroupApi) {
        this.locationGroupApi = locationGroupApi;
    }

    @Override
    protected boolean isTargetAvailable(LocationGroupVO target) {
        return target.isIncomingActive();
    }

    @Override
    protected Optional<LocationGroupVO> resolveTarget(RedirectVote vote) {
        return (vote.getTargetLocationGroup() == null || vote.getTargetLocationGroup().isEmpty()) ? Optional.empty() : locationGroupApi.findByName(vote.getTargetLocationGroup());
    }

    @Override
    protected void assignTarget(RedirectVote vote) {
        vote.getTransportOrder().setTargetLocationGroup(vote.getTargetLocationGroup());
    }
}
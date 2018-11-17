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

import org.openwms.common.CommonGateway;
import org.openwms.common.Location;
import org.openwms.common.LocationGroup;
import org.openwms.tms.TargetHandler;
import org.openwms.tms.TargetResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * A DefaultTargetResolvers.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
class DefaultTargetResolvers {

    @Component
    class LocationGroupTargetResolver implements TargetResolver<LocationGroup> {

        @Autowired
        private CommonGateway commonGateway;
        @Autowired
        private TargetHandler<LocationGroup> handler;

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<LocationGroup> resolve(String target) {
            return commonGateway.getLocationGroup(target);
        }

        @Override
        public TargetHandler<LocationGroup> getHandler() {
            return handler;
        }
    }

    @Component
    class LocationTargetResolver implements TargetResolver<Location> {

        @Autowired
        private CommonGateway commonGateway;
        @Autowired
        private TargetHandler<Location> handler;

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<Location> resolve(String target) {
            return commonGateway.getLocation(target);
        }

        @Override
        public TargetHandler<Location> getHandler() {
            return handler;
        }
    }
}

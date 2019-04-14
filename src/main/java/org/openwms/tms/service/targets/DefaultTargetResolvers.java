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
package org.openwms.tms.service.targets;

import org.openwms.common.location.api.LocationApi;
import org.openwms.common.location.api.LocationGroupApi;
import org.openwms.common.location.api.LocationGroupVO;
import org.openwms.common.location.api.LocationVO;
import org.openwms.tms.service.TargetHandler;
import org.openwms.tms.service.TargetResolver;
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
    class LocationGroupTargetResolver implements TargetResolver<LocationGroupVO> {

        private final LocationGroupApi locationGroupApi;
        private final TargetHandler<LocationGroupVO> handler;

        @Autowired
        public LocationGroupTargetResolver(LocationGroupApi locationGroupApi, TargetHandler<LocationGroupVO> handler) {
            this.locationGroupApi = locationGroupApi;
            this.handler = handler;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<LocationGroupVO> resolve(String target) {
            return locationGroupApi.findByName(target);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public TargetHandler<LocationGroupVO> getHandler() {
            return handler;
        }
    }

    @Component
    class LocationTargetResolver implements TargetResolver<LocationVO> {

        private final LocationApi locationApi;
        private final TargetHandler<LocationVO> handler;

        @Autowired
        public LocationTargetResolver(LocationApi locationApi, TargetHandler<LocationVO> handler) {
            this.locationApi = locationApi;
            this.handler = handler;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<LocationVO> resolve(String target) {
            return locationApi.findLocationByCoordinate(target);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public TargetHandler<LocationVO> getHandler() {
            return handler;
        }
    }
}

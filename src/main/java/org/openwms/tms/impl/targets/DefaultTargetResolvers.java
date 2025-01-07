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
package org.openwms.tms.impl.targets;

import org.ameba.annotation.Measured;
import org.openwms.common.location.api.LocationApi;
import org.openwms.common.location.api.LocationGroupApi;
import org.openwms.common.location.api.LocationGroupVO;
import org.openwms.common.location.api.LocationVO;
import org.openwms.tms.impl.TargetHandler;
import org.openwms.tms.impl.TargetResolver;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * A DefaultTargetResolvers.
 *
 * @author Heiko Scherrer
 */
class DefaultTargetResolvers {

    private DefaultTargetResolvers() { }

    @Component
    static class LocationGroupTargetResolver implements TargetResolver<LocationGroupVO> {

        private final LocationGroupApi locationGroupApi;
        private final TargetHandler<LocationGroupVO> handler;

        public LocationGroupTargetResolver(LocationGroupApi locationGroupApi, TargetHandler<LocationGroupVO> handler) {
            this.locationGroupApi = locationGroupApi;
            this.handler = handler;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Measured
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
    static class LocationTargetResolver implements TargetResolver<LocationVO> {

        private final LocationApi locationApi;
        private final TargetHandler<LocationVO> handler;

        public LocationTargetResolver(LocationApi locationApi, TargetHandler<LocationVO> handler) {
            this.locationApi = locationApi;
            this.handler = handler;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Measured
        public Optional<LocationVO> resolve(String target) {
            return locationApi.findById(target);
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

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
package org.openwms.common;

import feign.FeignException;
import feign.Response;
import feign.Util;
import org.ameba.Messages;
import org.ameba.exception.NotFoundException;
import org.ameba.exception.ServiceLayerException;
import org.ameba.mapping.BeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * A HttpCommonGateway.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@Component
class HttpCommonGateway implements CommonGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpCommonGateway.class);
    @Autowired
    private CommonFeignClient commonFeignClient;
    @Autowired
    private BeanMapper m;

    @Override
    public Optional<LocationGroup> getLocationGroup(String target) {
        try {
            LocationGroup lg = commonFeignClient.getLocationGroup(target);
            return Optional.of(lg);
        } catch (Exception ex) {
            int code = translate(ex);
            if (code == 404) {
                return Optional.empty();
            } else {
                LOGGER.error(ex.getMessage(), ex);
                throw new ServiceLayerException(ex.getMessage());
            }
        }
    }

    @Override
    public Optional<Location> getLocation(String target) {
        try {
            return Optional.ofNullable(commonFeignClient.getLocation(target));
        } catch (FeignException ex) {
            if (ex.status() == 404) {
                return Optional.empty();
            } else {
                LOGGER.error(ex.getMessage(), ex);
                throw new ServiceLayerException(ex.getMessage());
            }
        } catch (Exception ex) {
            int code = translate(ex);
            if (code == 404) {
                return Optional.empty();
            } else {
                LOGGER.error(ex.getMessage(), ex);
                throw new ServiceLayerException(ex.getMessage());
            }
        }
    }

    @Override
    public Optional<TransportUnit> getTransportUnit(String transportUnitBK) {
        try {
            TransportUnit tu = commonFeignClient.getTransportUnit(transportUnitBK);
            return Optional.of(tu);
        } catch (FeignException ex) {
            if (ex.status() == 404) {
                return Optional.empty();
            } else {
                LOGGER.error(ex.getMessage(), ex);
                throw new ServiceLayerException(ex.getMessage());
            }
        } catch (Exception ex) {
            if (translate(ex) == 404) {
                return Optional.empty();
            } else {
                LOGGER.error(ex.getMessage(), ex);
                throw new ServiceLayerException(ex.getMessage());
            }
        }
    }

    @Override
    public void updateTransportUnit(TransportUnit savedTU) {
        try {
            Response res = commonFeignClient.updateTU(savedTU.getBarcode(), m.map(savedTU, TransportUnitVO.class));
            String d = Util.toString(res.body().asReader());
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            if (translate(ex) == 404) {
                throw new NotFoundException(ex.getMessage(), Messages.NOT_FOUND, savedTU.getBarcode());
            } else {
                throw new ServiceLayerException(ex.getMessage());
            }
        }
    }

    private int translate(Exception ex) {
        if (ex.getCause() instanceof FeignException) {
            return ((FeignException) ex.getCause()).status();
        }
        LOGGER.error(ex.getMessage(), ex);
        throw new ServiceLayerException(ex.getMessage());
    }
}

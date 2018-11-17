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

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * A CommonFeignClient.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@FeignClient(name = "common-service", decode404 = true)
public interface CommonFeignClient {

    @GetMapping(value = "/v1/locations", params = {"locationPK"})
    Location getLocation(@RequestParam("locationPK") String locationPk);

    @GetMapping(value = "/v1/locations", params = {"locationGroupNames"})
    List<Location> getLocationsForLocationGroup(@RequestParam("locationGroupNames") String... locationGroupNames);

    @GetMapping(value = "/v1/locationgroups", params = {"name"})
    LocationGroup getLocationGroup(@RequestParam("name") String name);

    @GetMapping(value = "/v1/transportunits", params = {"bk"})
    TransportUnit getTransportUnit(@RequestParam("bk") String transportUnitBK);

    @GetMapping(value = "/v1/transportunits", params = {"actualLocation"})
    List<TransportUnit> getTransportUnitsOn(@RequestParam("actualLocation") String actualLocation);

    @PutMapping(value = "/v1/transportunits", params = {"bk"})
    Response updateTU(@RequestParam("bk") String transportUnitBK, @RequestBody TransportUnitVO tu);

    @GetMapping(value = "/stock", params = {"stockLocationGroupNames", "count"})
    List<Location> findStockLocationSimple(@RequestParam("stockLocationGroupNames") List<String> stockLocationGroupNames, @RequestParam("count") int count);
}

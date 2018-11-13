/*
 * openwms.org, the Open Warehouse Management System.
 * Copyright (C) 2014 Heiko Scherrer
 *
 * This file is part of openwms.org.
 *
 * openwms.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * openwms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.openwms.common;

import feign.Response;
import org.springframework.cloud.netflix.feign.FeignClient;
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

    @GetMapping(value = CommonConstants.API_LOCATIONS, params = {"locationPK"})
    Location getLocation(@RequestParam("locationPK") String locationPk);

    @GetMapping(value = CommonConstants.API_LOCATIONS, params = {"locationGroupNames"})
    List<Location> getLocationsForLocationGroup(@RequestParam("locationGroupNames") String... locationGroupNames);

    @GetMapping(value = CommonConstants.API_LOCATIONGROUPS, params = {"name"})
    LocationGroup getLocationGroup(@RequestParam("name") String name);

    @GetMapping(value = CommonConstants.API_TRANSPORTUNITS, params = {"bk"})
    TransportUnit getTransportUnit(@RequestParam("bk") String transportUnitBK);

    @GetMapping(value = CommonConstants.API_TRANSPORTUNITS, params = {"actualLocation"})
    List<TransportUnit> getTransportUnitsOn(@RequestParam("actualLocation") String actualLocation);

    @PutMapping(value = CommonConstants.API_TRANSPORTUNITS, params = {"bk"})
    Response updateTU(@RequestParam("bk") String transportUnitBK, @RequestBody TransportUnitVO tu);

    @GetMapping(value = "/stock", params = {"stockLocationGroupNames", "count"})
    List<Location> findStockLocationSimple(@RequestParam("stockLocationGroupNames") List<String> stockLocationGroupNames, @RequestParam("count") int count);
}

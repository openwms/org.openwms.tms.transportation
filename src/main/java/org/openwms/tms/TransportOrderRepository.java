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
package org.openwms.tms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * A TransportOrderRepository provides CRUD functionality regarding {@link TransportOrder} entity classes. It requires an existing transaction.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
public interface TransportOrderRepository extends JpaRepository<TransportOrder, Long> {

    @Query("select to from TransportOrder to where to.pKey = ?1")
    Optional<TransportOrder> findByPKey(String pKey);

    List<TransportOrder> findByTransportUnitBKIsInAndStateOrderByStartDate(List<String> transportUnitBKs, TransportOrderState state);

    List<TransportOrder> findByTargetLocationInAndStateAndSourceLocationIn(List<String> targetLocation, TransportOrderState state, List<String> sourceLocation);

    @Query("select to " +
            "from TransportOrder to " +
            "where to.targetLocation not in :targetLocations " +
            "and to.state = :state " +
            "and to.sourceLocation in :sourceLocations " +
            "order by to.priority desc, to.startDate, to.createDt")
    List<TransportOrder> findByTargetLocationGroupIsNotAndStateAndSourceLocationIn(
            @Param("targetLocations") List<String> targetLocations,
            @Param("state") TransportOrderState state,
            @Param("sourceLocations") List<String> sourceLocations);

    @Query("select to from TransportOrder to where to.pKey in ?1")
    List<TransportOrder> findByPKey(List<String> pKeys);

    @Query("select to " +
            "from TransportOrder to " +
            "where to.transportUnitBK = ?1 " +
            "and to.state in ?2 " +
            "order by to.priority desc, to.startDate, to.createDt")
    List<TransportOrder> findByTransportUnitBKAndStates(String transportUnitBK, TransportOrderState... states);

    List<TransportOrder> findByTargetLocation(String targetLocation);

    @Query("select count(to) from TransportOrder to where to.transportUnitBK = :transportUnitBK and to.state = :state")
    int numberOfTransportOrders(@Param("transportUnitBK") String transportUnitBK, @Param("state") TransportOrderState state);
}

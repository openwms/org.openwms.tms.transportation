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
package org.openwms.tms.impl;

import org.openwms.tms.TransportOrder;
import org.openwms.tms.TransportOrderState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * A TransportOrderRepository is an extension of Spring Data's {@link JpaRepository} that provides additional queries regarding
 * {@link TransportOrder} entities.
 *
 * @author Heiko Scherrer
 */
interface TransportOrderJpaRepository extends TransportOrderRepository<TransportOrder, Long>, JpaRepository<TransportOrder, Long> {

    @Query("""
              select to 
                from TransportOrder to 
               where to.pKey = ?1
               """)
    Optional<TransportOrder> findBypKey(String pKey);

    @Query("""
            select to 
              from TransportOrder to 
             where to.pKey in ?1
            """)
    List<TransportOrder> findBypKeys(List<String> pKeys);

    List<TransportOrder> findByTargetLocation(String targetLocation);

    @Query("""
                select to 
                  from TransportOrder to 
                 where to.transportUnitBK = ?1 
                   and to.state in ?2 
              order by to.priority desc, to.startDate, to.createDt
            """)
    List<TransportOrder> findByTransportUnitBKAndStates(String transportUnitBK, TransportOrderState... states);

    @Query("""
            select count(to) 
              from TransportOrder to 
             where to.transportUnitBK = :transportUnitBK 
               and to.state = :state
            """)
    int numberOfTransportOrders(@Param("transportUnitBK") String transportUnitBK, @Param("state") TransportOrderState state);
}

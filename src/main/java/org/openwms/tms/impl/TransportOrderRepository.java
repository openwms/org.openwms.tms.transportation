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
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * A TransportOrderRepository is an extension of Spring Data's {@link JpaRepository} that provides additional queries regarding
 * {@link TransportOrder} entities.
 *
 * @author Heiko Scherrer
 */
public interface TransportOrderRepository<T extends TransportOrder, ID extends Long> {

    T save(T to);

    <S extends T> S saveAndFlush(S entity);

    Optional<T> findById(ID pk);

    Optional<TransportOrder> findBypKey(String pKey);

    List<TransportOrder> findBypKeys(List<String> pKeys);

    List<TransportOrder> findByTargetLocation(String targetLocation);

    List<TransportOrder> findByTransportUnitBKAndStates(String transportUnitBK, TransportOrderState... states);

    int numberOfTransportOrders(@Param("transportUnitBK") String transportUnitBK, @Param("state") TransportOrderState state);
}

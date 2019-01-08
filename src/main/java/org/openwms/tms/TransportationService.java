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

import java.util.Collection;
import java.util.List;

/**
 * A TransportationService offers some useful methods regarding the general handling of {@link TransportOrder}s.
 *
 * @param <T> Any kind of {@link TransportOrder}
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
public interface TransportationService<T extends TransportOrder> {

    /**
     * FInd all {@link TransportOrder}s for a {@code TransportUnit} in the given
     * {@code states}.
     * 
     * @param barcode The Barcode of the TransportUnit
     * @param states A set of TransportOrder states
     * @return A List of all TransportOrders, never {@literal null}
     */
    List<T> findBy(String barcode, String... states);

    /**
     * Find and return the {@link TransportOrder} identified by the persisted key {@code pKey}.
     *
     * @param pKey The persisted key
     * @return An {@link TransportOrder} instance
     * @throws org.ameba.exception.NotFoundException if no entity was found
     */
    T findByPKey(String pKey);

    /**
     * Find and return all {@link TransportOrder}s with the given {@code state} and the
     * {@code sourceLocation} to the {@code targetLocationGroupNames} as target.
     *
     * @param state The state
     * @param sourceLocation Name of the sourceLocation, probably the infeed position
     * @param targetLocationGroupNames Name of the targetLocationGroups
     * @return All TransportOrders or an empty collection, never {@literal null}
     */
    List<TransportOrder> findInfeed(TransportOrderState state, String sourceLocation, String... targetLocationGroupNames);

    /**
     * Find and return all {@link TransportOrder}s with the given {@code state} and the
     * {@code sourceLocationGroupName} and {@code targetLocationGroupName}.
     *
     * @param state The state
     * @param sourceLocationGroupName Name of the source LocationGroup
     * @param targetLocationGroupName Name of the target LocationGroup
     * @return All TransportOrders or an empty collection, never {@literal null}
     */
    List<T> findInAisle(TransportOrderState state, String sourceLocationGroupName, String targetLocationGroupName);

    /**
     * Find and return all {@link TransportOrder}s with the given {@code state} and the
     * {@code sourceLocationGroupNames} as source.
     *
     * @param state The state
     * @param sourceLocationGroupNames Name of the source LocationGroups
     * @return All TransportOrders or an empty collection, never {@literal null}
     */
    List<T> findOutfeed(TransportOrderState state, String... sourceLocationGroupNames);

    /**
     * Returns the number of {@link TransportOrder}s that have the {@code target} as target and are in one of the {@code states}.
     *
     * @param target The target place to search TransportOrders for
     * @param states An array of TransportOrder states to filter TransportOrders for
     * @return Number of all TransportOrders in one of the {@code states} that are on the way to the {@code target}
     */
    int getNoTransportOrdersToTarget(String target, String... states);

    /**
     * Create a new {@link TransportOrder} with the given {@code target}.
     *
     * @param barcode The ID of the {@code TransportUnit} to move
     * @param target The target of the TransportOrder to move to
     * @param priority A {@link PriorityLevel} of the new {@link TransportOrder}
     * @return The newly created {@link TransportOrder}
     */
    T create(String barcode, String target, String priority);

    /**
     * Modifies an existing {@link TransportOrder} according to the argument passed as {@link TransportOrder}.
     *
     * @param transportOrder Stores the ID of the {@link TransportOrder} to change as well as the state to change
     * @return The modified instance
     */
    T update(T transportOrder);

    /**
     * Try to turn a list of {@link TransportOrder}s into the given {@code state}.
     *
     * @param state The state to change all orders to
     * @param pKeys The persisted keys of {@link TransportOrder}s
     * @return A list of persisted keys of {@link TransportOrder}s that have not been changed
     * @deprecated Use update instead
     */
    @Deprecated
    Collection<String> change(TransportOrderState state, Collection<String> pKeys);
}
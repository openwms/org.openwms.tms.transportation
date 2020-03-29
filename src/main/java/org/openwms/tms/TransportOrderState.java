/*
 * Copyright 2005-2020 the original author or authors.
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

/**
 * A TransportOrderState defines all possible states a {@link TransportOrder} may resist in.
 *
 * @author Heiko Scherrer
 */
public enum TransportOrderState {

    /** Status of new created {@code TransportOrder}s. */
    CREATED(10),

    /** Status of a full initialized {@code TransportOrder}, ready to be started. */
    INITIALIZED(20),

    /** A started and active{@code TransportOrder}, ready to be executed. */
    STARTED(30),

    /** Status to indicate that the {@code TransportOrder} is paused. Not active anymore. */
    INTERRUPTED(40),

    /** Status to indicate a failure on the {@code TransportOrder}. Not active anymore. */
    ONFAILURE(50),

    /** Status of a aborted {@code TransportOrder}. Not active anymore. */
    CANCELED(60),

    /** Status to indicate that the {@code TransportOrder} completed successfully. */
    FINISHED(70);

    private final int order;

    TransportOrderState(int sortOrder) {
        this.order = sortOrder;
    }

    /**
     * Get the order.
     *
     * @return the order.
     */
    public int getOrder() {
        return order;
    }
}

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
package org.openwms.tms.impl.state;

import org.openwms.tms.TransportOrder;

/**
 * A Startable implementation is able to start or restart {@code TransportOrder}s.
 *
 * @author Heiko Scherrer
 */
public interface Startable {

    /**
     * Start the {@code TransportOrder} with the given id.
     *
     * @param pKey The persistent key of the TransportOrder to start
     */
    void start(String pKey);

    /**
     * Start the next {@code TransportOrder} for the {@code TransportUnit}.
     *
     * @param transportUnitBK The business key of the {@code TransportUnit} to start
     */
    void startNext(String transportUnitBK);

    /**
     * Trigger a start of the given {@code TransportOrder}.
     *
     * @param to The TransportOrder instance to start
     */
    void triggerStart(TransportOrder to);
}

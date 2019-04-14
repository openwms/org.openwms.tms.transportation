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
package org.openwms.tms.service;

import org.openwms.common.location.api.Target;

/**
 * A TargetHandler offers a set of functions according to the specific of the {@code Target} implementation.
 *
 * @param <T> some kind of {@code Target}
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
public interface TargetHandler<T extends Target> {

    /**
     * Get the number of {@code TransportOrder}s that are on the way to the specific {@code Target}.
     *
     * @param target The target to search for
     * @return The number of TransportOrders
     */
    int getNoTOToTarget(T target);
}

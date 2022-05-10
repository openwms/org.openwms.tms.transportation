/*
 * Copyright 2005-2022 the original author or authors.
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
 * A StateManager is able to manage the state of a {@link TransportOrder}.
 *
 * @author Heiko Scherrer
 */
public interface StateManager {

    /**
     * Validates if the requested state transition to {@code newState} is valid or not. Does not change the state of the given
     * {@code transportOrder.}
     *
     * @param newState The new state
     * @param transportOrder The TransportOrder to check the state transition for
     * @throws StateChangeException in case of errors
     */
    void validate(TransportOrderState newState, TransportOrder transportOrder);
}

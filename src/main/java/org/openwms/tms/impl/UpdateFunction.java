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

/**
 * A UpdateFunction.
 *
 * @author Heiko Scherrer
 */
public interface UpdateFunction {

    /**
     * Validate and change the requested values of the {@code saved} instance.
     *
     * @param saved The currently persisted TransportOrder
     * @param toUpdate The TransportOrder holding the new values to save
     */
    void update(TransportOrder saved, TransportOrder toUpdate);
}

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
package org.openwms.tms.impl;

import org.openwms.tms.TransportOrder;
import org.springframework.stereotype.Component;

/**
 * A PrioritizeTO is responsible to change the priority of a {@link TransportOrder}.
 *
 * @author Heiko Scherrer
 */
@Component
class PrioritizeTO implements UpdateFunction {

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(TransportOrder saved, TransportOrder toUpdate) {
        if (saved.getPriority() != toUpdate.getPriority() && toUpdate.getPriority() != null) {

            // Request to change priority
            saved.setPriority(toUpdate.getPriority());
        }
    }
}

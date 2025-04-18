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

import org.ameba.annotation.Measured;
import org.openwms.core.SpringProfiles;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * A InternalStartAcceptor.
 *
 * @author Heiko Scherrer
 */
@Profile(SpringProfiles.NOT_ASYNCHRONOUS)
@Lazy
@Component
class InternalStartAcceptor implements ExternalStarter {

    private final Startable startable;

    InternalStartAcceptor(Startable startable) {
        this.startable = startable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Measured
    public void request(String pKey) {
        startable.start(pKey);
    }
}

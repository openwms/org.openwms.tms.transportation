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
package org.openwms.tms.impl.state;

import org.openwms.tms.StateManager;
import org.openwms.tms.TransportOrder;
import org.openwms.tms.TransportServiceEvent;
import org.openwms.tms.impl.UpdateFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A ChangeState is an {@link UpdateFunction} to change the state of an {@link TransportOrder}.
 *
 * @author Heiko Scherrer
 * @see UpdateFunction
 */
@Transactional(propagation = Propagation.MANDATORY)
@Component
class ChangeState implements UpdateFunction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeState.class);
    private final ApplicationContext ctx;
    private final StateManager stateManager;

    ChangeState(ApplicationContext ctx, StateManager stateManager) {
        this.ctx = ctx;
        this.stateManager = stateManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(TransportOrder saved, TransportOrder toUpdate) {
        if (saved.getState() != toUpdate.getState() && toUpdate.getState() != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Trying to turn TransportOrder [{}] into state [{}]", saved.getPk(), toUpdate.getState());
            }
            saved.changeState(stateManager, toUpdate.getState());
            ctx.publishEvent(new TransportServiceEvent(saved, TransportServiceEvent.TYPE.of(toUpdate.getState())));
        }
    }
}

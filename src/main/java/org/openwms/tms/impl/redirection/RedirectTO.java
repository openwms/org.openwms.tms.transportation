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
package org.openwms.tms.impl.redirection;

import org.openwms.tms.Message;
import org.openwms.tms.TransportOrder;
import org.openwms.tms.impl.AddProblem;
import org.openwms.tms.impl.UpdateFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * A RedirectTO is responsible to handle target changes of a {@link TransportOrder}. Only the {@code targetLocationGroup} of the
 * TransportOrder {@code toUpdate} is recognized.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@Component
class RedirectTO implements UpdateFunction {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectTO.class);
    /** 0..* voters, can be overridden and extended with XML configuration. So far we define only one (default) voter directly. */
    private final List<DecisionVoter<RedirectVote>> redirectVoters;
    private final AddProblem addProblem;

    public RedirectTO(@Autowired(required = false) List<DecisionVoter<RedirectVote>> redirectVoters, AddProblem addProblem) {
        this.redirectVoters = redirectVoters;
        this.addProblem = addProblem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(TransportOrder saved, TransportOrder toUpdate) {

        if (null != redirectVoters && differentTarget(saved, toUpdate)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Redirecting TO with pKey [{}] to targetLocation [{}] and targetLocationGroup [{}]", saved.getPersistentKey(), toUpdate.getTargetLocation(), toUpdate.getTargetLocationGroup());
            }
            RedirectVote rv = new RedirectVote(toUpdate.getTargetLocation(), toUpdate.getTargetLocationGroup(), saved);
            // CHECK [openwms]: 13/07/16 the concept of a voter is misused in that a voter changes the state of a TO
            for (DecisionVoter<RedirectVote> voter : redirectVoters) {
                voter.voteFor(rv);
            }

            if (rv.hasMessages()) {
                rv.getMessages().forEach(m -> addProblem.add(new Message.Builder().withMessage(m.getMessage()).build(), saved));
            }

            if (!rv.completed()) {
                throw new DeniedException("TransportOrder couldn't be redirected to a new Target");
            }
        }
    }

    private boolean differentTarget(TransportOrder saved, TransportOrder toUpdate) {
        return (
            saved.hasTargetLocationGroup() && !saved.getTargetLocationGroup().equals(toUpdate.getTargetLocationGroup()) && toUpdate.getTargetLocationGroup() != null ||
            !saved.hasTargetLocationGroup() && toUpdate.hasTargetLocationGroup() ||
            saved.hasTargetLocation() && !saved.getTargetLocation().equals(toUpdate.getTargetLocation()) && toUpdate.getTargetLocation() != null ||
            !saved.hasTargetLocation() && toUpdate.hasTargetLocation()
        );
    }
}

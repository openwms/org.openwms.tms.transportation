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

import org.ameba.i18n.Translator;
import org.openwms.common.location.api.TargetVO;
import org.openwms.tms.DeniedException;
import org.openwms.tms.Message;
import org.openwms.tms.TMSMessageCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * A TargetRedirector.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
abstract class TargetRedirector<T extends TargetVO> implements DecisionVoter<RedirectVote> {

    @Autowired
    private Translator translator;
    private static final Logger LOGGER = LoggerFactory.getLogger(TargetRedirector.class);

    /**
     * The implementation has to vote for a certain vote on particular rules that are implemented by the voter.
     *
     * @param vote The vote to vote for
     * @throws DeniedException is thrown when the voter cannot vote for the action
     */
    @Override
    public void voteFor(RedirectVote vote) throws DeniedException {
        Optional<T> optionalTarget = resolveTarget(vote);
        if (optionalTarget.isPresent()) {
            if (isTargetAvailable(optionalTarget.get())) {
                assignTarget(vote);
                vote.complete();
            } else {
                String msg = translator.translate(TMSMessageCodes.TARGET_BLOCKED_MSG, vote.getTargetLocation(), vote.getTargetLocationGroup(), vote.getTransportOrder().getPersistentKey());
                vote.addMessage(new Message.Builder().withMessage(msg).withMessageNo(TMSMessageCodes.TARGET_BLOCKED_MSG).build());
                LOGGER.info(msg);
            }
        }
    }

    protected abstract boolean isTargetAvailable(T target);

    protected abstract Optional<T> resolveTarget(RedirectVote vote);

    protected abstract void assignTarget(RedirectVote vote);
}

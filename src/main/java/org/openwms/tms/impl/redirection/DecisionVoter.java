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
package org.openwms.tms.impl.redirection;

import org.openwms.tms.DeniedException;

/**
 * A DecisionVoter is asked to vote for a business action.
 *
 * @param <T> Any type of Vote
 * @author Heiko Scherrer
 */
interface DecisionVoter<T extends Vote> {

    /**
     * The implementation has to vote for a certain vote on particular rules that are implemented by the voter.
     *
     * @param vote The vote to vote for
     * @throws DeniedException is thrown when the voter cannot vote for the action
     */
    void voteFor(T vote) throws DeniedException;
}

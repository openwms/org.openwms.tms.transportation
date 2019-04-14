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

import org.openwms.tms.AddProblem;
import org.openwms.tms.Message;
import org.openwms.tms.ProblemHistory;
import org.openwms.tms.TransportOrder;
import org.openwms.tms.UpdateFunction;
import org.openwms.tms.internal.ProblemHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A AddProblemImpl.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@Transactional(propagation = Propagation.MANDATORY)
@Component
class AddProblemImpl implements UpdateFunction, AddProblem {

    private final ProblemHistoryRepository repository;

    @Autowired
    public AddProblemImpl(ProblemHistoryRepository repository) {
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(TransportOrder saved, TransportOrder toUpdate) {
        if (saved.hasProblem() && toUpdate.hasProblem() && !saved.getProblem().equals(toUpdate.getProblem()) ||
                !saved.hasProblem() && toUpdate.hasProblem()) {

            // A Problem occurred and must be added to the TO ...
            add(toUpdate.getProblem(), saved);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(Message problem, TransportOrder transportOrder) {
        if (transportOrder.hasProblem()) {
            repository.save(new ProblemHistory(transportOrder, transportOrder.getProblem()));
        }
        transportOrder.setProblem(problem);
    }
}

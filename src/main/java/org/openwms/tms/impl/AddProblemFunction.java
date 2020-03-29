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
package org.openwms.tms.impl;

import org.ameba.annotation.TxService;
import org.openwms.tms.Message;
import org.openwms.tms.ProblemHistory;
import org.openwms.tms.TransportOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;

/**
 * A AddProblemImpl.
 *
 * @author Heiko Scherrer
 */
@TxService(propagation = Propagation.MANDATORY)
class AddProblemFunction implements UpdateFunction {

    private final ProblemHistoryRepository repository;

    @Autowired
    public AddProblemFunction(ProblemHistoryRepository repository) {
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
            addInternal(toUpdate.getProblem(), saved);
        }
    }

    // Internal because of TX Aspects
    private void addInternal(Message problem, TransportOrder transportOrder) {
        if (transportOrder.hasProblem()) {
            repository.save(new ProblemHistory(transportOrder, transportOrder.getProblem()));
        }
        transportOrder.setProblem(problem);
    }
}

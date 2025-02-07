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
package org.openwms.tms;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.ameba.integration.jpa.BaseEntity;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A ProblemHistory stores an occurred problem, in form of {@code Message}, recorded on {@code TransportOrder}.
 *
 * @author Heiko Scherrer
 */
@Entity
@Table(name = "TMS_PROBLEM_HISTORY")
public class ProblemHistory extends BaseEntity implements Serializable {

    @ManyToOne
    @JoinColumn(name = "C_FK_TO")
    private TransportOrder transportOrder;
    @Embedded
    private Message problem;

    /** Dear JPA ... */
    protected ProblemHistory() {}

    /**
     * Full constructor.
     *
     * @param transportOrder The TO this problem initially occurred
     * @param problem The problem itself
     */
    public ProblemHistory(TransportOrder transportOrder, Message problem) {
        this.transportOrder = transportOrder;
        this.problem = problem;
    }

    /**
     * Get the problem.
     *
     * @return The problem
     */
    public Message getProblem() {
        return problem;
    }

    /**
     * Get the corresponding {@code TransportOrder}.
     *
     * @return The transportOrder
     */
    public TransportOrder getTransportOrder() {
        return transportOrder;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ProblemHistory.class.getSimpleName() + "[", "]")
                .add("transportOrderPKey=" + transportOrder.getPersistentKey())
                .add("problem=" + problem)
                .toString();
    }

    /**
     * All fields.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProblemHistory that = (ProblemHistory) o;
        return Objects.equals(transportOrder, that.transportOrder) && Objects.equals(problem, that.problem);
    }

    /**
     * All fields.
     */
    @Override
    public int hashCode() {
        return Objects.hash(transportOrder, problem);
    }
}

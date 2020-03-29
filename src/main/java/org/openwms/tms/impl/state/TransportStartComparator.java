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

import org.openwms.tms.PriorityLevel;
import org.openwms.tms.TransportOrder;

import java.io.Serializable;
import java.util.Comparator;

/**
 * A TransportStartComparator. I used to sort TransportOrders is a particular order. Unfortunately some fields of the TransportOrder class
 * are defined as Enums for a better handling in business logic. Persisting these fields as Strings makes it impossible to do a proper
 * sorting in the database with JPA. Hence we must do it with Comparators in the application layer.
 * 
 * @author Heiko Scherrer
 * @see PriorityLevel
 */
class TransportStartComparator implements Comparator<TransportOrder>, Serializable {

    /**
     * First the priority or orders is compared, when both are equals the id is compared too.
     * 
     * @param o1
     *            FirstOrder to compare
     * @param o2
     *            Second order to compare
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(TransportOrder o1, TransportOrder o2) {
        if (o1.getPriority().getOrder() > o2.getPriority().getOrder()) {
            return -1;
        } else if (o1.getPriority().getOrder() < o2.getPriority().getOrder()) {
            return 1;
        }
        if (o1.getPk() < o2.getPk()) {
            return -1;
        } else if (o1.getPk() > o2.getPk()) {
            return 1;
        }
        return 0;
    }
}
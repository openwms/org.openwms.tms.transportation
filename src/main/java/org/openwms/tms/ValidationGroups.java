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
package org.openwms.tms;

/**
 * A ValidationGroups is a collection of marker interfaces used for Bean Validation with
 * JSR-303.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
public interface ValidationGroups {

    /** Validate that a transportUnitBK and at least one target exist. */
    interface ValidateBKAndTarget{}

    /** Validation that is applied when a {@code TransportOrder} is created. */
    interface OrderCreation{}

    /** Validation that is applied when a {@code TransportOrder} is updated. */
    interface OrderUpdate {}
}

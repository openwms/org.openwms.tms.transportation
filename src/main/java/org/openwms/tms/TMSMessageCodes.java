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
 * A TMSMessageCodes is a collection with message codes unique within this module.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
public final class TMSMessageCodes {

    private TMSMessageCodes() {
    }

    /*~ Messagetext Codes */


    /** Signals that the TransportOrder with the given persisted key wasn't found. */
    public static final String TO_WITH_PKEY_NOT_FOUND = "TMS.TO_WITH_PKEY_NOT_FOUND";
    /** Signals an exception because it was tried to turn back a TransportOrder into a state that isn't allowed. */
    public static final String TO_STATE_CHANGE_BACKWARDS_NOT_ALLOWED = "TMS.TO_STATE_CHANGE_BACKWARDS_NOT_ALLOWED";
    /** Signals an exception because it was tried to change the state of a TransportOrder into a following but not allowed state. */
    public static final String TO_STATE_CHANGE_NOT_READY = "TMS.TO_STATE_CHANGE_NOT_READY";
    /** Signals that a request with state of NULL. */
    public static final String TO_STATE_CHANGE_NULL_STATE = "TMS.TO_STATE_CHANGE_NULL_STATE";
    /** Signals an exception that it is not allowed to start a TransportOrder, because there is already a started one. */
    public static final String START_TO_NOT_ALLOWED_ALREADY_STARTED_ONE = "TMS.START_TO_NOT_ALLOWED_ALREADY_STARTED_ONE";
    /** Signals an exception that the requested state change is not allowed for the initialized TransportOrder. */
    public static final String STATE_CHANGE_ERROR_FOR_INITIALIZED_TO="TMS.STATE_CHANGE_ERROR_FOR_INITIALIZED_TO";


    /*~ Message Codes */

    /** Target LocationGroup or Location is blocked for infeed. */
    public static final String TARGET_BLOCKED_MSG = "TMS.TARGET_BLOCKED";
}

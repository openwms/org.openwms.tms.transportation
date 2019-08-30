/*
 * Copyright 2005-2019 the original author or authors.
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
package org.openwms.tms.api.requests.state;

import org.openwms.tms.api.MessageVO;

import java.beans.ConstructorProperties;
import java.io.Serializable;

/**
 * A StateChangeResponse.
 *
 * @author Heiko Scherrer
 */
public final class StateChangeResponse implements Serializable {

    /** The identifying persistent key of the TransportOrder to change. */
    private StateChangeRequest request;
    /** The accepted state the TransportOrder can be switched to. */
    private String acceptedState;
    /** An optional error message. */
    private MessageVO error;

    @ConstructorProperties({"request", "acceptedState", "error"})
    public StateChangeResponse(StateChangeRequest request, String acceptedState, MessageVO error) {
        // Client API shall be framework neutral!
        if (request == null) {
            throw new IllegalArgumentException("Argument request is null when creating a StateChangeResponse");
        }
        this.request = request;
        this.acceptedState = acceptedState;
        this.error = error;
    }

    public StateChangeRequest getRequest() {
        return request;
    }

    public boolean hasRequest() {
        return this.request != null;
    }

    public void setRequest(StateChangeRequest request) {
        this.request = request;
    }

    public String getAcceptedState() {
        return acceptedState;
    }

    public void setAcceptedState(String acceptedState) {
        this.acceptedState = acceptedState;
    }

    public MessageVO getError() {
        return error;
    }

    public boolean hasError() {
        return this.error != null;
    }

    public void setError(MessageVO error) {
        this.error = error;
    }
}

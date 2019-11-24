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

import org.ameba.exception.ServiceLayerException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A DeniedException is thrown by a {@code DecisionVoter}s in case a business action is not allowed to be executed.
 * 
 * @author Heiko Scherrer
 */
@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.CONFLICT)
public class DeniedException extends ServiceLayerException {

    private DeniedException(String message) {
        super(message);
    }

    /**
     * Create a new DeniedException.
     * 
     * @param message Detail message
     * @param cause Root cause
     */
    public DeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Factory method to create one with a message text.
     *
     * @param message Detail message
     * @return A new instance
     */
    public static final DeniedException with(String message) {
        return new DeniedException(message);
    }
}
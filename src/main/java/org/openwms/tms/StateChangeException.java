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

import org.ameba.exception.BehaviorAwareException;
import org.ameba.i18n.Translator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;

/**
 * A StateChangeException signals that the request to change the state of a {@code TransportOrder} was not allowed.
 *
 * @author Heiko Scherrer
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StateChangeException extends BehaviorAwareException {

    public StateChangeException(String message) {
        super(message);
    }

    public StateChangeException(String message, String msgKey, Serializable... data) {
        super(message, msgKey, data);
    }

    public StateChangeException(Translator translator, String messageKey, Serializable... param) {
        super(translator.translate(messageKey, (Object[]) param), messageKey, param);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}

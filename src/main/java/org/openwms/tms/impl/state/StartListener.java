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
package org.openwms.tms.impl.state;

import org.ameba.exception.NotFoundException;
import org.ameba.i18n.Translator;
import org.openwms.tms.TransportServiceEvent;
import org.openwms.tms.impl.TransportOrderRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static org.openwms.tms.TMSMessageCodes.TO_WITH_PK_NOT_FOUND;

/**
 * A StartListener.
 *
 * @author Heiko Scherrer
 */
@Service
class StartListener {

    private final TransportOrderRepository repository;
    private final Startable starter;
    private final Translator translator;

    StartListener(TransportOrderRepository repository, Startable starter, Translator translator) {
        this.repository = repository;
        this.starter = starter;
        this.translator = translator;
    }

    @EventListener
    public void onEvent(TransportServiceEvent event) {
        final var pk = event.getSource().getPk();
        var to = repository.findById(pk).orElseThrow(
                () -> new NotFoundException(translator, TO_WITH_PK_NOT_FOUND, new Long[]{pk}, pk)
        );
        switch (event.getType()) {
            case INITIALIZED
                    -> starter.triggerStart(to);
            case TRANSPORT_FINISHED, TRANSPORT_ONFAILURE, TRANSPORT_CANCELED, TRANSPORT_INTERRUPTED
                    -> starter.startNext(to.getTransportUnitBK());
            default -> {
            // just accept the evolution here
            }
        }
    }
}

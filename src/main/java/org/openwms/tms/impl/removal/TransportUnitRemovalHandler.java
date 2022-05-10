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
package org.openwms.tms.impl.removal;

import org.ameba.annotation.Measured;
import org.ameba.annotation.TxService;
import org.openwms.common.transport.api.commands.TUCommand;
import org.openwms.tms.Message;
import org.openwms.tms.StateChangeException;
import org.openwms.tms.StateManager;
import org.openwms.tms.TransportOrder;
import org.openwms.tms.impl.TransportOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.openwms.tms.TransportOrderState.CANCELED;
import static org.openwms.tms.TransportOrderState.CREATED;
import static org.openwms.tms.TransportOrderState.FINISHED;
import static org.openwms.tms.TransportOrderState.INITIALIZED;
import static org.openwms.tms.TransportOrderState.ONFAILURE;
import static org.openwms.tms.TransportOrderState.STARTED;

/**
 * A TransportUnitRemovalHandler updates existing TransportOrders in case a TransportUnit
 * is deleted.
 *
 * @author Heiko Scherrer
 */
@TxService
class TransportUnitRemovalHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransportUnitRemovalHandler.class);
    private final TransportOrderRepository repository;
    private final ApplicationContext ctx;
    private final List<String> blockStates;
    private final StateManager stateManager;

    TransportUnitRemovalHandler(TransportOrderRepository repository,
            ApplicationContext ctx,
            @Value("${owms.tms.block-tu-deletion-states}") String cancelStartedTO, StateManager stateManager) {
        this.repository = repository;
        this.ctx = ctx;
        this.blockStates = cancelStartedTO == null ?
                Collections.emptyList() :
                Stream.of(cancelStartedTO
                        .split(","))
                        .map(String::trim)
                        .toList();
        this.stateManager = stateManager;
    }

    @Transactional
    @Measured
    public void preRemove(TUCommand command) throws RemovalNotAllowedException {
        Assert.notNull(command, "Not allowed to call preRemove with null argument");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The TransportUnit with pKey [{}] is going to be removed. Check for existing TransportOrders",
                    command.getTransportUnit().getpKey());
        }
        try {
            checkForStarted(command);
            cancelInitializedOrders(command);
            unlinkFinishedOrders(command);
            unlinkCanceledOrders(command);
            command.setType(TUCommand.Type.REMOVE);
            ctx.publishEvent(command);
        } catch (IllegalStateException ise) {
            throw new RemovalNotAllowedException(ise.getMessage(), ise);
        }
    }

    protected void checkForStarted(TUCommand command) {
        var transportOrders = repository.findByTransportUnitBKAndStates(
                command.getTransportUnit().getBarcode(),
                STARTED
        );

        if (blockStates.contains(STARTED.toString()) &&
                !transportOrders.isEmpty()) {

            throw new IllegalStateException("STARTED TransportOrders exist, removal not allowed");
        }
        if (transportOrders.isEmpty()) {

            LOGGER.debug("No STARTED TransportOrders found");
        } else {

            transportOrders.forEach(this::cancel);
        }
    }

    private void cancel(TransportOrder transportOrder) {
        try {
            transportOrder.changeState(stateManager, CANCELED);
            var barcode = transportOrder.getTransportUnitBK();
            setProblem(transportOrder,
                    new Message.Builder()
                            .withMessageText(format("TransportUnit with ID [%s] was deleted and Transport Order canceled", barcode))
                            .build()
            );
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Successfully unlinked and canceled TransportOrder with PK [{}]", transportOrder.getPk());
            }
        } catch (StateChangeException sce) {
            transportOrder.setProblem(
                    new Message.Builder()
                            .withMessageText(sce.getMessage())
                            .build()
            );
        } finally {
            repository.save(transportOrder);
        }
    }

    private void setProblem(TransportOrder transportOrder, Message problem) {
        transportOrder.setProblem(problem);
        repository.save(transportOrder);
    }

    protected void cancelInitializedOrders(TUCommand command) {
        var transportOrders = repository.findByTransportUnitBKAndStates(
                command.getTransportUnit().getBarcode(),
                CREATED,
                INITIALIZED
        );

        if ((blockStates.contains(INITIALIZED.toString()) ||
                (blockStates.contains(CREATED.toString()))) &&
                !transportOrders.isEmpty()) {

            throw new IllegalStateException("CREATED or INITIALIZED TransportOrders exist, removal not allowed");
        }
        if (transportOrders.isEmpty()) {

            LOGGER.debug("No CREATED or INITIALIZED TransportOrders found");
        } else {

            transportOrders.forEach(this::cancel);
        }
    }

    protected void unlinkFinishedOrders(TUCommand command) {
        var transportOrders = repository.findByTransportUnitBKAndStates(
                command.getTransportUnit().getBarcode(),
                FINISHED,
                ONFAILURE
        );

        if ((blockStates.contains(FINISHED.toString()) ||
                (blockStates.contains(ONFAILURE.toString()))) &&
                !transportOrders.isEmpty()) {

            throw new IllegalStateException("FINISHED or ONFAILURE TransportOrders exist, removal not allowed");
        }
        if (transportOrders.isEmpty()) {

            LOGGER.debug("No FINISHED and ONFAILURE TransportOrders found to unlink");
        } else {

            transportOrders.forEach(to -> {
                setProblem(to,
                        new Message.Builder()
                                .withMessageText(
                                        format("TransportUnit with barcode [%s] was removed and TransportOrder unlinked",
                                                command.getTransportUnit().getBarcode()
                                        )
                                )
                                .build());
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Successfully unlinked TransportOrder with PK [{}]",
                            to.getPk());
                }
            });
        }
    }

    protected void unlinkCanceledOrders(TUCommand command) {
        var transportOrders = repository.findByTransportUnitBKAndStates(
                command.getTransportUnit().getBarcode(),
                CANCELED
        );
        if (transportOrders.isEmpty()) {

            LOGGER.debug("No CANCELED TransportOrders found to unlink");
        } else {

            transportOrders.forEach(to -> {
                setProblem(to,
                        new Message.Builder()
                                .withMessageText(format("TransportUnit with barcode [%s] was removed and TransportOrder unlinked",
                                        command.getTransportUnit().getBarcode()))
                                .build());
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Successfully unlinked canceled TransportOrder with PK [{}]", to.getPk());
                }
            });
        }
    }
}
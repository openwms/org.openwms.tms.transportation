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
package org.openwms.tms.impl;

import org.ameba.annotation.Measured;
import org.ameba.annotation.TxService;
import org.ameba.exception.NotFoundException;
import org.ameba.i18n.Translator;
import org.openwms.common.location.LocationPK;
import org.openwms.common.location.api.TargetVO;
import org.openwms.tms.Message;
import org.openwms.tms.PriorityLevel;
import org.openwms.tms.StateChangeException;
import org.openwms.tms.StateManager;
import org.openwms.tms.TMSMessageCodes;
import org.openwms.tms.TransportOrder;
import org.openwms.tms.TransportOrderState;
import org.openwms.tms.TransportServiceEvent;
import org.openwms.tms.TransportationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * A TransportationServiceImpl is a Spring managed transactional service.
 *
 * @author Heiko Scherrer
 */
@TxService
class TransportationServiceImpl implements TransportationService<TransportOrder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransportationServiceImpl.class);

    private final Translator translator;
    private final TransportOrderRepository repository;
    private final ApplicationContext ctx;
    private final StateManager stateManager;
    private final List<UpdateFunction> updateFunctions;
    private final List<TargetResolver<TargetVO>> targetResolvers;

    TransportationServiceImpl(Translator translator, TransportOrderRepository repository, ApplicationContext ctx,
            StateManager stateManager, @Autowired(required = false) List<UpdateFunction> updateFunctions,
            @Autowired(required = false) List<TargetResolver<TargetVO>> targetResolvers) {
        this.translator = translator;
        this.repository = repository;
        this.ctx = ctx;
        this.stateManager = stateManager;
        this.updateFunctions = updateFunctions;
        this.targetResolvers = targetResolvers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Measured
    public List<TransportOrder> findBy(String barcode, String... states) {
        return repository.findByTransportUnitBKAndStates(barcode,
                Stream.of(states)
                        .map(TransportOrderState::valueOf)
                        .toList()
                        .toArray(new TransportOrderState[states.length])
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Measured
    public TransportOrder findByPKey(String pKey) {
        return findBy(pKey);
    }

    private TransportOrder findBy(String pKey) {
        return repository.findBypKey(pKey).orElseThrow(() -> new NotFoundException(translator, TMSMessageCodes.TO_WITH_PKEY_NOT_FOUND, new String[]{pKey}, pKey));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Measured
    public int getNoTransportOrdersToTarget(String target, String... states) {
        int i = 0;
        for (var tr : targetResolvers) {
            var t = tr.resolve(target);
            if (t.isPresent()) {
                i = +tr.getHandler().getNoTOToTarget(t.get());
            }
        }
        return i;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks that all necessary data to create a TransportOrder is given, does not do any logical checks, whether a target is blocked or a
     * {@link TransportOrder} for the {@code TransportUnit} exist.
     *
     * @throws NotFoundException when the barcode is {@literal null} or no transportunit with barcode can be found or no target
     * can be found.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = StateChangeException.class)
    @Measured
    public TransportOrder create(String barcode, String target, String priority) {
        if (barcode == null) {
            throw new NotFoundException("Barcode cannot be null when creating a TransportOrder");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Trying to create TransportOrder with Barcode [{}], to Target [{}], with Priority [{}]", barcode, target, priority);
        }
        var transportOrder = new TransportOrder(barcode);
        if (LocationPK.isValid(target)) {
            transportOrder.setTargetLocation(target);
        } else {
            transportOrder.setTargetLocationGroup(target);
        }
        if (priority != null && !priority.isEmpty()) {
            transportOrder.setPriority(PriorityLevel.of(priority));
        } else {
            transportOrder.setPriority(PriorityLevel.NORMAL);
        }
        transportOrder = repository.saveAndFlush(transportOrder);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("TransportOrder for Barcode [{}] created. PKey is [{}], PK is [{}]", barcode, transportOrder.getPersistentKey(), transportOrder.getPk());
        }
        ctx.publishEvent(new TransportServiceEvent(transportOrder, TransportServiceEvent.TYPE.TRANSPORT_CREATED));
        transportOrder = repository.findBypKey(transportOrder.getPersistentKey()).orElseThrow(NotFoundException::new);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("TransportOrder for Barcode [{}] persisted. PKey is [{}], PK is [{}]", barcode, transportOrder.getPersistentKey(), transportOrder.getPk());
        }
        return transportOrder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Measured
    public TransportOrder update(TransportOrder transportOrder) {
        var saved = findBy(transportOrder.getPersistentKey());
        updateFunctions.forEach(up -> up.update(saved, transportOrder));
        return repository.save(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Measured
    public Collection<String> change(TransportOrderState state, Collection<String> pKeys) {
        var failure = new ArrayList<String>(pKeys.size());
        var transportOrders = repository.findBypKeys(new ArrayList<>(pKeys));
        for (var transportOrder : transportOrders) {
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Trying to turn TransportOrder [{}] into state [{}]", transportOrder.getPk(), state);
                }
                transportOrder.changeState(stateManager, state);
                ctx.publishEvent(new TransportServiceEvent(transportOrder, TransportServiceEvent.TYPE.of(state)));
            } catch (StateChangeException sce) {
                LOGGER.error("Could not turn TransportOrder: [{}] into [{}], because of [{}]", transportOrder.getPk(), state, sce.getMessage());
                var problem = new Message.Builder().withMessage(sce.getMessage()).build();
                transportOrder.setProblem(problem);
                failure.add(transportOrder.getPk().toString());
            }
        }
        return failure;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Measured
    public Collection<Message> change(String barcode, TransportOrderState currentState, TransportOrderState targetState, Message message) {
        var transportOrders = repository.findByTransportUnitBKAndStates(barcode, currentState);
        var failure = new ArrayList<Message>();
        for (var transportOrder : transportOrders) {
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Trying to turn TransportOrder [{}] into state [{}]", transportOrder.getPk(), targetState);
                }
                transportOrder.changeState(stateManager, targetState);
                if (message != null) {
                    transportOrder.setProblem(message);
                }
                ctx.publishEvent(new TransportServiceEvent(transportOrder, TransportServiceEvent.TYPE.of(targetState)));
            } catch (StateChangeException sce) {
                LOGGER.error("Could not turn TransportOrder: [{}] into [{}], because of [{}]", transportOrder.getPk(), targetState, sce.getMessage());
                var problem = new Message.Builder()
                        .withMessage(sce.getMessage())
                        .withMessageNo(sce.getMessageKey())
                        .withPKey(transportOrder.getPersistentKey())
                        .build();
                transportOrder.setProblem(problem);
                failure.add(problem);
            }
        }
        return failure;
    }
}
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
package org.openwms.tms.service;

import org.ameba.annotation.Measured;
import org.ameba.annotation.TxService;
import org.ameba.exception.NotFoundException;
import org.ameba.i18n.Translator;
import org.openwms.common.location.api.Target;
import org.openwms.tms.Message;
import org.openwms.tms.PriorityLevel;
import org.openwms.tms.StateChangeException;
import org.openwms.tms.TMSMessageCodes;
import org.openwms.tms.TargetResolver;
import org.openwms.tms.TransportOrder;
import org.openwms.tms.TransportOrderState;
import org.openwms.tms.TransportServiceEvent;
import org.openwms.tms.TransportationService;
import org.openwms.tms.UpdateFunction;
import org.openwms.tms.internal.TransportOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A TransportationServiceImpl is a Spring managed transactional service.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@TxService
class TransportationServiceImpl implements TransportationService<TransportOrder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransportationServiceImpl.class);

    private final ApplicationContext ctx;
    private final TransportOrderRepository repository;
    @Autowired(required = false)
    private List<TargetResolver<Target>> targetResolvers;
    @Autowired(required = false)
    private List<UpdateFunction> updateFunctions;
    private final Translator translator;

    TransportationServiceImpl(Translator translator, TransportOrderRepository repository, ApplicationContext ctx) {
        this.translator = translator;
        this.repository = repository;
        this.ctx = ctx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Measured
    public List<TransportOrder> findBy(String barcode, String... states) {
        return repository.findByTransportUnitBKAndStates(barcode, Stream.of(states).map(TransportOrderState::valueOf).collect(Collectors.toList()).toArray(new TransportOrderState[states.length]));
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
        return repository.findByPKey(pKey).orElseThrow(() -> new NotFoundException(translator, TMSMessageCodes.TO_WITH_PKEY_NOT_FOUND, new String[]{pKey}, pKey));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Measured
    public int getNoTransportOrdersToTarget(String target, String... states) {
        int i = 0;
        for (TargetResolver<Target> tr : targetResolvers) {
            Optional<Target> t = tr.resolve(target);
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
     *                           can be found.
     */
    @Override
    @Measured
    public TransportOrder create(String barcode, String target, String priority) {
        if (barcode == null) {
            throw new NotFoundException("Barcode cannot be null when creating a TransportOrder");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Trying to create TransportOrder with Barcode [{}], to Target [{}], with Priority [{}]", barcode, target, priority);
        }
        TransportOrder transportOrder = new TransportOrder(barcode).setTargetLocation(target).setTargetLocationGroup(target);
        if (priority != null && !priority.isEmpty()) {
            transportOrder.setPriority(PriorityLevel.of(priority));
        } else {
            transportOrder.setPriority(PriorityLevel.NORMAL);
        }
        transportOrder = repository.save(transportOrder);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("TransportOrder for Barcode [{}] created. PKey is [{}], PK is [{}]", barcode, transportOrder.getPersistentKey(), transportOrder.getPk());
        }
        ctx.publishEvent(new TransportServiceEvent(transportOrder, TransportServiceEvent.TYPE.TRANSPORT_CREATED));
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
        TransportOrder saved = findBy(transportOrder.getPersistentKey());
        updateFunctions.forEach(up -> up.update(saved, transportOrder));
        return repository.save(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Measured
    public Collection<String> change(TransportOrderState state, Collection<String> pKeys) {
        List<String> failure = new ArrayList<>(pKeys.size());
        List<TransportOrder> transportOrders = repository.findByPKey(new ArrayList<>(pKeys));
        for (TransportOrder transportOrder : transportOrders) {
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Trying to turn TransportOrder [{}] into state [{}]", transportOrder.getPk(), state);
                }
                transportOrder.changeState(state);
                ctx.publishEvent(new TransportServiceEvent(transportOrder.getPk(), TransportServiceEvent.TYPE.of(state)));
            } catch (StateChangeException sce) {
                LOGGER.error("Could not turn TransportOrder: [{}] into [{}], because of [{}]", transportOrder.getPk(), state, sce.getMessage());
                Message problem = new Message.Builder().withMessage(sce.getMessage()).build();
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
    public Collection<String> change(String barcode, TransportOrderState currentState, TransportOrderState targetState, Message message) {
        List<TransportOrder> transportOrders = repository.findByTransportUnitBKAndStates(barcode, currentState);
        List<String> failure = new ArrayList<>();
        for (TransportOrder transportOrder : transportOrders) {
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Trying to turn TransportOrder [{}] into state [{}]", transportOrder.getPk(), targetState);
                }
                transportOrder.changeState(targetState);
                if (message != null) {
                    transportOrder.setProblem(message);
                }
                ctx.publishEvent(new TransportServiceEvent(transportOrder, TransportServiceEvent.TYPE.of(targetState)));
            } catch (StateChangeException sce) {
                LOGGER.error("Could not turn TransportOrder: [{}] into [{}], because of [{}]", transportOrder.getPk(), targetState, sce.getMessage());
                Message problem = new Message.Builder().withMessage(sce.getMessage()).build();
                transportOrder.setProblem(problem);
                failure.add(transportOrder.getPk().toString());
            }
        }
        return failure;
    }
}
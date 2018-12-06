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
package org.openwms.tms.removal;

import org.ameba.annotation.TxService;
import org.openwms.common.transport.api.TransportUnitVO;
import org.openwms.tms.Message;
import org.openwms.tms.StateChangeException;
import org.openwms.tms.TransportOrder;
import org.openwms.tms.TransportOrderRepository;
import org.openwms.tms.TransportOrderState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.List;

/**
 * A TransportUnitRemovalListener is asked to allow or disallow the removal of a TransportUnit in a distributed system.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@TxService
class TransportUnitRemovalListener implements OnRemovalListener<TransportUnitVO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransportUnitRemovalListener.class);
    private final TransportOrderRepository repository;

    @Autowired
    public TransportUnitRemovalListener(TransportOrderRepository repository) {
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The implementation verifies that no active {@link TransportOrder}s exist, before a {@link TransportUnitVO} is going to be removed. <ul>
     * <li>In case where already 'started' {@link TransportOrder}s exist it is not allowed to remove the {@link TransportUnitVO} therefore an
     * exception is thrown.</li> <li>If {@link TransportOrder}s in a state less than 'started' exist they will be canceled and removed. The
     * removal of the {@link TransportUnitVO} is accepted.</li> </ul>
     *
     * @throws RemovalNotAllowedException when active {@link TransportOrder}s exist for the {@link TransportUnitVO} entity.
     */
    @Override
    public void preRemove(TransportUnitVO entity) throws RemovalNotAllowedException {
        Assert.notNull(entity, "Not allowed to call preRemove with null argument");
        LOGGER.debug("Someone is trying to remove the TransportUnit [{}], check for existing TransportOrders", entity);
        try {
            cancelInitializedOrders(entity);
            unlinkFinishedOrders(entity);
            unlinkCanceledOrders(entity);
        } catch (IllegalStateException ise) {
            LOGGER.warn("For one or more created TransportOrders it is not allowed to cancel them");
            throw new RemovalNotAllowedException(
                    "For one or more created TransportOrders it is not allowed to cancel them");
        }
    }

    protected void cancelInitializedOrders(TransportUnitVO transportUnit) {
        LOGGER.debug("Trying to cancel and remove already created but not started TransportOrders");
        List<TransportOrder> transportOrders = repository.findByTransportUnitBKAndStates(transportUnit.getBarcode(), TransportOrderState.CREATED,
                TransportOrderState.INITIALIZED);
        if (!transportOrders.isEmpty()) {
            for (TransportOrder transportOrder : transportOrders) {
                try {
                    transportOrder.changeState(TransportOrderState.CANCELED);
                    transportOrder.setProblem(new Message.Builder().withMessage("TransportUnit " + transportUnit
                            + " was removed, order was canceled").build());
                    transportOrder.setTransportUnitBK(null);
                    LOGGER.debug("Successfully unlinked and canceled TransportOrder [{}]", transportOrder.getPk());
                } catch (StateChangeException sce) {
                    transportOrder.setProblem(new Message.Builder().withMessage(sce.getMessage()).build());
                } finally {
                    repository.save(transportOrder);
                }
            }
        }
    }

    protected void unlinkFinishedOrders(TransportUnitVO transportUnit) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Trying to unlink finished and failed TransportOrders for TransportUnit: " + transportUnit);
        }
        List<TransportOrder> transportOrders = repository.findByTransportUnitBKAndStates(transportUnit.getBarcode(), TransportOrderState.FINISHED,
                TransportOrderState.ONFAILURE);
        if (!transportOrders.isEmpty()) {
            for (TransportOrder transportOrder : transportOrders) {
                transportOrder.setProblem(new Message.Builder().withMessage("TransportUnit " + transportUnit
                        + " was removed, order was unlinked").build());
                transportOrder.setTransportUnitBK(null);
                repository.save(transportOrder);
                LOGGER.debug("Successfully unlinked TransportOrder [{}]", transportOrder.getPk());
            }
        }
    }

    protected void unlinkCanceledOrders(TransportUnitVO transportUnit) {
        List<TransportOrder> transportOrders = repository.findByTransportUnitBKAndStates(transportUnit.getBarcode(), TransportOrderState.CANCELED);
        if (!transportOrders.isEmpty()) {
            for (TransportOrder transportOrder : transportOrders) {
                transportOrder.setProblem(new Message.Builder().withMessage("TransportUnit " + transportUnit
                        + " was removed, order was unlinked").build());
                transportOrder.setTransportUnitBK(null);
                repository.save(transportOrder);
                LOGGER.debug("Successfully unlinked canceled TransportOrder [{}]", transportOrder.getPk());
            }
        }
    }
}
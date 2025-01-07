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

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import org.ameba.integration.jpa.ApplicationEntity;
import org.openwms.tms.api.ValidationGroups;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A TransportOrder is used to move {@code TransportUnit}s from the current {@code Location} to another target (Location).
 *
 * @author Heiko Scherrer
 */
@Entity
@Table(name = "TMS_TRANSPORT_ORDER")
public class TransportOrder extends ApplicationEntity implements Serializable {

    /**
     * The bk of the {@code TransportUnit} to be moved by this {@code TransportOrder}. Allowed to be {@literal null} to keep {@code
     * TransportOrder}s without {@code TransportUnit}s.
     */
    @Column(name = "C_TRANSPORT_UNIT_BK")
    @Min(value = 1, groups = ValidationGroups.ValidateBKAndTarget.class)
    private String transportUnitBK;

    /**
     * A priority level of the {@code TransportOrder}. The lower the value the lower the priority.<br> The priority level affects the
     * execution of the {@code TransportOrder}. An order with high priority will be processed faster than those with lower priority.
     */
    @Column(name = "C_PRIORITY")
    @Enumerated(EnumType.STRING)
    private PriorityLevel priority = PriorityLevel.NORMAL;

    /**
     * Date when the {@code TransportOrder} was started.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "C_START_DATE")
    private Date startDate;

    /**
     * Last reported problem on the {@code TransportOrder}.
     */
    @Embedded
    private Message problem;

    /**
     * Date when the {@code TransportOrder} ended.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "C_END_DATE")
    private Date endDate;

    /**
     * State of the {@code TransportOrder}.
     */
    @Column(name = "C_STATE")
    @Enumerated(EnumType.STRING)
    private TransportOrderState state = TransportOrderState.CREATED;

    /**
     * The source {@code Location} of the {@code TransportOrder}.<br> This property is set before the {@code TransportOrder} was started.
     */
    @Column(name = "C_SOURCE_LOCATION")
    private String sourceLocation;

    /**
     * The target {@code Location} of the {@code TransportOrder}.<br> This property is set before the {@code TransportOrder} was started.
     */
    @Column(name = "C_TARGET_LOCATION")
    private String targetLocation;

    /**
     * A {@code LocationGroup} can also be set as target. At least one target must be set when the {@code TransportOrder} is being started.
     */
    @Column(name = "C_TARGET_LOCATION_GROUP")
    @NotEmpty(groups = ValidationGroups.ValidateBKAndTarget.class)
    private String targetLocationGroup;

    /* ----------------------------- constructors ------------------- */

    /** Dear JPA... */
    protected TransportOrder() {}

    /**
     * Create a TransportOrder with the given TransportUnit's business key.
     *
     * @param transportUnitBK TransportUnit business key
     */
    public TransportOrder(String transportUnitBK) {
        this.transportUnitBK = transportUnitBK;
    }

    /*~ ----------------------------- methods ------------------- */

    @Override
    public void setPersistentKey(String pKey) {
        super.setPersistentKey(pKey);
    }

    /**
     * Returns the priority level of the {@code TransportOrder}.
     *
     * @return The priority
     */
    public PriorityLevel getPriority() {
        return this.priority;
    }

    /**
     * Set the priority level of the {@code TransportOrder}.
     *
     * @param priority The priority to set
     */
    public void setPriority(PriorityLevel priority) {
        this.priority = priority;
    }

    /**
     * Returns the date when the {@code TransportOrder} was started.
     *
     * @return The date when started
     */
    public Date getStartDate() {
        return this.startDate;
    }

    /**
     * Set the date when the TransportOrder has been activated for processing.
     *
     * @param startDate The start date
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Get the {@code TransportUnit} assigned to the {@code TransportOrder} .
     *
     * @return The business key of the assigned {@code TransportUnit}
     */
    public String getTransportUnitBK() {
        return this.transportUnitBK;
    }

    /**
     * Assign a {@code TransportUnit} to the {@code TransportOrder}. Setting the {@code TransportUnit} to {@literal null} is allowed here to
     * unlink both.
     *
     * @param transportUnitBK The business key of the {@code TransportUnit} to be assigned
     */
    public void setTransportUnitBK(String transportUnitBK) {
        this.transportUnitBK = transportUnitBK;
    }

    /**
     * Check whether this {@code TransportOrder} has a {@code TransportUnit}'s business key set.
     *
     * @return {@code true} if a the business key is assigne
     */
    public boolean hasTransportUnitBK() {
        return this.transportUnitBK != null && !this.transportUnitBK.isEmpty();
    }

    /**
     * Returns the state of the {@code TransportOrder}.
     *
     * @return The state of the order
     */
    public TransportOrderState getState() {
        return state;
    }

    /**
     * Used for MapStruct only.
     *
     * @param state The state to set
     */
    public void setState(TransportOrderState state) {
        this.state = state;
    }

    /**
     * Change the state of the {@code TransportOrder} regarding some rules.
     *
     * @param newState The new state of the order
     * @return The modified instance
     * @throws StateChangeException in case <ul> <li>the newState is {@literal null} or</li> <li>the newState is less than the old state
     * or</li> <li>the {@code TransportOrder} is in state {@link TransportOrderState#CREATED} and shall be manually turned into something
     * else then {@link TransportOrderState#INITIALIZED} or {@link TransportOrderState#CANCELED}</li> <li>the {@code TransportOrder} is
     * {@link TransportOrderState#CREATED} and shall be {@link TransportOrderState#INITIALIZED} but it is incomplete</li> </ul>
     */
    public TransportOrder changeState(StateManager stateManager, TransportOrderState newState) {
        stateManager.validate(newState, this);
        state = newState;
        return this;
    }

    /**
     * Get the target {@code Location} of this {@code TransportOrder}.
     *
     * @return The targetLocation if any, otherwise {@literal null}
     */
    public String getTargetLocation() {
        return targetLocation;
    }

    public boolean hasTargetLocation() {
        return this.targetLocation != null && !this.targetLocation.isEmpty();
    }

    /**
     * Set the target {@code Location} of this {@code TransportOrder}.
     *
     * @param targetLocation The location to move on
     * @return this
     */
    public TransportOrder setTargetLocation(String targetLocation) {
        this.targetLocation = targetLocation;
        return this;
    }

    /**
     * Get the targetLocationGroup.
     *
     * @return The targetLocationGroup if any, otherwise {@literal null}
     */
    public String getTargetLocationGroup() {
        return targetLocationGroup;
    }

    public boolean hasTargetLocationGroup() {
        return this.targetLocationGroup != null && !this.targetLocationGroup.isEmpty();
    }

    /**
     * Set the targetLocationGroup.
     *
     * @param targetLocationGroup The targetLocationGroup to set.
     * @return this
     */
    public TransportOrder setTargetLocationGroup(String targetLocationGroup) {
        this.targetLocationGroup = targetLocationGroup;
        return this;
    }

    /**
     * Get the last {@link Message}.
     *
     * @return The last problem.
     */
    public Message getProblem() {
        return problem;
    }

    /**
     * Set the last {@link Message}.
     *
     * @param problem The {@link Message} to set.
     * @return this
     */
    public TransportOrder setProblem(Message problem) {
        this.problem = problem;
        return this;
    }

    /**
     * Get the endDate.
     *
     * @return The date the order ended
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Set the date when the TransportOrder has been deactivated for processing.
     *
     * @param endDate The end date
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Get the sourceLocation.
     *
     * @return The sourceLocation
     */
    public String getSourceLocation() {
        return sourceLocation;
    }

    /**
     * Set the sourceLocation.
     *
     * @param sourceLocation The sourceLocation to set
     * @return this
     */
    public TransportOrder setSourceLocation(String sourceLocation) {
        this.sourceLocation = sourceLocation;
        return this;
    }

    /**
     * Check whether a problem was reported on this TO.
     *
     * @return {@literal true} if so, otherwise {@literal false}
     */
    public boolean hasProblem() {
        return problem != null;
    }

    /**
     * Check whether one of the targets has changed between this TransportOrder and the one passed as {@code transportOrder}.
     *
     * @param transportOrder The TransportOrder to verify against
     * @return {@literal true} if targets has changed, otherwise {@literal false}
     */
    boolean hasTargetChanged(TransportOrder transportOrder) {
        return ((targetLocation != null && !targetLocation.equals(transportOrder.getTargetLocation())) ||
                (targetLocationGroup != null && targetLocationGroup.equals(transportOrder.getTargetLocationGroup())));
    }

    /**
     * {@inheritDoc}
     *
     * All fields.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransportOrder that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(transportUnitBK, that.transportUnitBK) && priority == that.priority && Objects.equals(startDate, that.startDate) && Objects.equals(problem, that.problem) && Objects.equals(endDate, that.endDate) && state == that.state && Objects.equals(sourceLocation, that.sourceLocation) && Objects.equals(targetLocation, that.targetLocation) && Objects.equals(targetLocationGroup, that.targetLocationGroup);
    }

    /**
     * {@inheritDoc}
     *
     * All fields.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), transportUnitBK, priority, startDate, problem, endDate, state, sourceLocation, targetLocation, targetLocationGroup);
    }

    /**
     * {@inheritDoc}
     *
     * All fields.
     */
    @Override
    public String toString() {
        return new StringJoiner(", ", TransportOrder.class.getSimpleName() + "[", "]")
                .add("transportUnitBK='" + transportUnitBK + "'")
                .add("priority=" + priority)
                .add("startDate=" + startDate)
                .add("problem=" + problem)
                .add("endDate=" + endDate)
                .add("state=" + state)
                .add("sourceLocation='" + sourceLocation + "'")
                .add("targetLocation='" + targetLocation + "'")
                .add("targetLocationGroup='" + targetLocationGroup + "'")
                .toString();
    }
}

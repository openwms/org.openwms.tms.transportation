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
package org.openwms.tms.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * A UpdateTransportOrderVO.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
public class UpdateTransportOrderVO implements Serializable {

    @NotEmpty(groups = ValidationGroups.OrderUpdate.class)
    private String pKey;
    private String barcode;
    private String priority;
    @JsonProperty
    private MessageVO problem;
    private String state;
    private String targetLocation;
    private String targetLocationGroup;
    private String actualLocation;

    UpdateTransportOrderVO() {
    }

    private UpdateTransportOrderVO(Builder builder) {
        setpKey(builder.pKey);
        setBarcode(builder.barcode);
        setPriority(builder.priority);
        setProblem(builder.problem);
        setState(builder.state);
        setTargetLocation(builder.targetLocation);
        setTargetLocationGroup(builder.targetLocationGroup);
        setActualLocation(builder.actualLocation);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getpKey() {
        return pKey;
    }

    public void setpKey(String pKey) {
        this.pKey = pKey;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public MessageVO getProblem() {
        return problem;
    }

    public void setProblem(MessageVO problem) {
        this.problem = problem;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(String targetLocation) {
        this.targetLocation = targetLocation;
    }

    public String getTargetLocationGroup() {
        return targetLocationGroup;
    }

    public void setTargetLocationGroup(String targetLocationGroup) {
        this.targetLocationGroup = targetLocationGroup;
    }

    public String getActualLocation() {
        return actualLocation;
    }

    public void setActualLocation(String actualLocation) {
        this.actualLocation = actualLocation;
    }

    @Override
    public String toString() {
        return "UpdateTransportOrderVO{" + "pKey='" + pKey + '\'' + ", barcode='" + barcode + '\'' + ", priority='" + priority + '\'' + ", problem=" + problem + ", state='" + state + '\'' + ", targetLocation='" + targetLocation + '\'' + ", targetLocationGroup='" + targetLocationGroup + '\'' + ", actualLocation='" + actualLocation + '\'' + '}';
    }

    public static final class Builder {
        private String pKey;
        private String barcode;
        private String priority;
        private MessageVO problem;
        private String state;
        private String targetLocation;
        private String targetLocationGroup;
        private String actualLocation;

        private Builder() {
        }

        public Builder withPKey(String val) {
            pKey = val;
            return this;
        }

        public Builder withBarcode(String val) {
            barcode = val;
            return this;
        }

        public Builder withPriority(String val) {
            priority = val;
            return this;
        }

        public Builder withProblem(MessageVO val) {
            problem = val;
            return this;
        }

        public Builder withState(String val) {
            state = val;
            return this;
        }

        public Builder withTargetLocation(String val) {
            targetLocation = val;
            return this;
        }

        public Builder withTargetLocationGroup(String val) {
            targetLocationGroup = val;
            return this;
        }

        public Builder withActualLocation(String val) {
            actualLocation = val;
            return this;
        }

        public UpdateTransportOrderVO build() {
            return new UpdateTransportOrderVO(this);
        }
    }
}

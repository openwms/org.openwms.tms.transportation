/*
 * Copyright 2005-2020 the original author or authors.
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
 * A CreateTransportOrderVO.
 *
 * @author Heiko Scherrer
 */
public class CreateTransportOrderVO implements Serializable {

    @NotEmpty
    private String pKey;
    @NotEmpty
    private String barcode;
    private String priority;
    @JsonProperty
    private MessageVO problem;
    private String state;
    @NotEmpty
    @JsonProperty("targetLocation")
    private String target;

    @JsonProperty("targetLocationGroup")
    public String getTargetLocationGroup() {
        return target;
    }
    public CreateTransportOrderVO() {

    }
    private CreateTransportOrderVO(Builder builder) {
        setpKey(builder.pKey);
        setBarcode(builder.barcode);
        setPriority(builder.priority);
        setProblem(builder.problem);
        setState(builder.state);
        setTarget(builder.target);
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

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "CreateTransportOrderVO{" +
                "pKey='" + pKey + '\'' +
                ", barcode='" + barcode + '\'' +
                ", priority='" + priority + '\'' +
                ", problem=" + problem +
                ", state='" + state + '\'' +
                ", target='" + target + '\'' +
                '}';
    }


    public static final class Builder {
        private @NotEmpty String pKey;
        private @NotEmpty String barcode;
        private String priority;
        private MessageVO problem;
        private String state;
        private @NotEmpty String target;

        private Builder() {
        }

        public Builder withPKey(@NotEmpty String val) {
            pKey = val;
            return this;
        }

        public Builder withBarcode(@NotEmpty String val) {
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

        public Builder withTarget(@NotEmpty String val) {
            target = val;
            return this;
        }

        public CreateTransportOrderVO build() {
            return new CreateTransportOrderVO(this);
        }
    }
}

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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A MessageVO.
 *
 * @author <a href="mailto:hscherrer@interface21.io">Heiko Scherrer</a>
 */
public class MessageVO implements Serializable {

    @JsonProperty
    private Date occurred;
    @JsonProperty
    private String messageNo;
    @JsonProperty
    private String message;

    /*~-------------------- constructors --------------------*/
    private MessageVO(Builder builder) {
        occurred = builder.occurred;
        messageNo = builder.messageNo;
        message = builder.message;
    }

    @JsonCreator
    protected MessageVO() {
    }

    /*~-------------------- accessors --------------------*/
    public static Builder newBuilder() {
        return new Builder();
    }

    public Date getOccurred() {
        return occurred;
    }

    public String getMessageNo() {
        return messageNo;
    }

    public String getMessage() {
        return message;
    }

    /*~-------------------- builder --------------------*/
    public static final class Builder {
        private Date occurred;
        private String messageNo;
        private String message;

        private Builder() {
        }

        public Builder occurred(Date val) {
            occurred = val;
            return this;
        }

        public Builder messageNo(String val) {
            messageNo = val;
            return this;
        }

        public Builder message(String val) {
            message = val;
            return this;
        }

        public MessageVO build() {
            if (this.occurred == null) {
                this.occurred = new Date();
            }
            return new MessageVO(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MessageVO messageVO = (MessageVO) o;
        return Objects.equals(occurred, messageVO.occurred) && Objects.equals(messageNo, messageVO.messageNo) && Objects.equals(message, messageVO.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(occurred, messageNo, message);
    }

    /**
     * {@inheritDoc}
     *
     * Use all fields.
     */
    @Override
    public String toString() {
        return new StringJoiner(", ", MessageVO.class.getSimpleName() + "[", "]")
                .add("occurred=" + occurred)
                .add("messageNo='" + messageNo + "'")
                .add("message='" + message + "'")
                .toString();
    }
}

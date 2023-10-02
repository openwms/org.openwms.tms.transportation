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
package org.openwms.tms.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A MessageVO.
 *
 * @author Heiko Scherrer
 */
public class MessageVO implements Serializable {

    @JsonProperty("occurred")
    private LocalDateTime occurred;
    @JsonProperty("messageNo")
    private String messageNo;
    @JsonProperty("messageText")
    private String messageText;

    /*~-------------------- constructors --------------------*/
    private MessageVO(Builder builder) {
        occurred = builder.occurred;
        messageNo = builder.messageNo;
        messageText = builder.messageText;
    }

    @JsonCreator
    public MessageVO() {}

    /*~-------------------- accessors --------------------*/
    public static Builder newBuilder() {
        return new Builder();
    }

    public LocalDateTime getOccurred() {
        return occurred;
    }

    public String getMessageNo() {
        return messageNo;
    }

    public String getMessageText() {
        return messageText;
    }

    /*~-------------------- builder --------------------*/
    public static class Builder {
        private LocalDateTime occurred;
        private String messageNo;
        private String messageText;

        private Builder() {
        }

        public Builder occurred(LocalDateTime val) {
            occurred = val;
            return this;
        }

        public Builder messageNo(String val) {
            messageNo = val;
            return this;
        }

        public Builder messageText(String val) {
            messageText = val;
            return this;
        }

        public MessageVO build() {
            if (this.occurred == null) {
                this.occurred = LocalDateTime.now();
            }
            return new MessageVO(this);
        }
    }

    /**
     * {@inheritDoc}
     *
     * All fields.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MessageVO messageVO = (MessageVO) o;
        return Objects.equals(occurred, messageVO.occurred) && Objects.equals(messageNo, messageVO.messageNo) && Objects.equals(messageText, messageVO.messageText);
    }

    /**
     * {@inheritDoc}
     *
     * All fields.
     */
    @Override
    public int hashCode() {
        return Objects.hash(occurred, messageNo, messageText);
    }

    /**
     * {@inheritDoc}
     *
     * All fields.
     */
    @Override
    public String toString() {
        return new StringJoiner(", ", MessageVO.class.getSimpleName() + "[", "]")
                .add("occurred=" + occurred)
                .add("messageNo='" + messageNo + "'")
                .add("messageText='" + messageText + "'")
                .toString();
    }
}

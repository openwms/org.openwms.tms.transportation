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
package org.openwms.tms;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A Message is used to encapsulate a message text with an identifier.
 * 
 * @GlossaryTerm
 * @author Heiko Scherrer
 */
@Embeddable
public class Message implements Serializable {

    /** Timestamp when the {@literal Message} has occurred. */
    @Column(name = "C_OCCURRED")
    private LocalDateTime occurred;

    /** Message number of the {@literal Message}. */
    @Column(name = "C_NO")
    private String messageNo;

    /** Message description text. */
    @Column(name = "C_MESSAGE_TEXT")
    private String messageText;

    /** The unique key of the domain object in that context the message occurred. */
    private String pKey;

    /*~ ----------------------------- constructors ------------------- */

    /**
     * Dear JPA...
     */
    protected Message() {}

    private Message(Builder builder) {
        occurred = builder.occurred;
        messageNo = builder.messageNo;
        messageText = builder.messageText;
        pKey = builder.pKey;
    }

    public static Message.Builder newBuilder() {
        return new Message.Builder();
    }

    /**
     * Return the Date when the {@literal Message} has occurred.
     * 
     * @return Date when occurred.
     */
    public LocalDateTime getOccurred() {
        return occurred;
    }

    /**
     * Get the messageNo.
     * 
     * @return The messageNo.
     */
    public String getMessageNo() {
        return messageNo;
    }

    /**
     * Return the message text.
     *
     * @return The message text
     */
    public String getMessageText() {
        return messageText;
    }

    /**
     * Get the pKey.
     *
     * @return The pKey.
     */
    public String getpKey() {
        return pKey;
    }

    /**
     * {@inheritDoc}
     *
     * Use all fields.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var message1 = (Message) o;
        return Objects.equals(occurred, message1.occurred) &&
                Objects.equals(messageNo, message1.messageNo) &&
                Objects.equals(messageText, message1.messageText) &&
                Objects.equals(pKey, message1.pKey);
    }

    /**
     * {@inheritDoc}
     *
     * Use all fields.
     */
    @Override
    public String toString() {
        return new StringJoiner(", ", Message.class.getSimpleName() + "[", "]")
                .add("occurred=" + occurred)
                .add("messageNo='" + messageNo + "'")
                .add("messageText='" + messageText + "'")
                .toString();
    }

    /**
     * {@inheritDoc}
     *
     * Use all fields.
     */
    @Override
    public int hashCode() {
        return Objects.hash(occurred, messageNo, messageText, pKey);
    }


    /**
     * {@code Message} builder static inner class.
     */
    public static class Builder {

        private LocalDateTime occurred;
        private String messageNo;
        private String messageText;
        private String pKey;

        /**
         * Sets the {@code occurred} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param val the {@code occurred} to set
         * @return a reference to this Builder
         */
        public Builder occurred(LocalDateTime val) {
            occurred = val;
            return this;
        }

        /**
         * Sets the {@code messageNo} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param val the {@code messageNo} to set
         * @return a reference to this Builder
         */
        public Builder messageNo(String val) {
            messageNo = val;
            return this;
        }

        /**
         * Sets the {@code messageText} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param val the {@code messageText} to set
         * @return a reference to this Builder
         */
        public Builder messageText(String val) {
            messageText = val;
            return this;
        }

        /**
         * Sets the {@code pKey} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param val the {@code pKey} to set
         * @return a reference to this Builder
         */
        public Builder pKey(String val) {
            pKey = val;
            return this;
        }

        /**
         * Returns a {@code Message} built from the parameters previously set.
         *
         * @return a {@code Message} built with parameters of this {@code Message.Builder}
         */
        public Message build() {
            return new Message(this);
        }
    }
}
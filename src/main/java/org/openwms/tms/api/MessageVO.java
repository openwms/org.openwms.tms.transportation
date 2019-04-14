/*
 * Copyright 2019 Heiko Scherrer
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

import java.util.Date;

/**
 * A MessageVO.
 *
 * @author <a href="mailto:hscherrer@interface21.io">Heiko Scherrer</a>
 */
public class MessageVO {

    private Date occurred;
    private String messageNo;
    private String message;

    private MessageVO(Builder builder) {
        occurred = builder.occurred;
        messageNo = builder.messageNo;
        message = builder.message;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

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
            return new MessageVO(this);
        }
    }
}

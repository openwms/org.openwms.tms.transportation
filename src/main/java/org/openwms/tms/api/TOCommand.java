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

import java.io.Serializable;

/**
 * A TOCommand is used to trigger actions on {@code TransportOrder}s.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
public class TOCommand implements Serializable {

    private CreateTransportOrderVO createTransportOrder;
    private UpdateTransportOrderVO updateTransportOrder;
    private Type type;

    private TOCommand(Builder builder) {
        setCreateTransportOrder(builder.createTransportOrder);
        setUpdateTransportOrder(builder.updateTransportOrder);
        setType(builder.type);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public enum Type {
        CREATE,
        CHANGE_TARGET,
        CHANGE_ACTUAL_LOCATION
    }

    protected TOCommand() {
    }

    public CreateTransportOrderVO getCreateTransportOrder() {
        return createTransportOrder;
    }

    public void setCreateTransportOrder(CreateTransportOrderVO createTransportOrder) {
        this.createTransportOrder = createTransportOrder;
    }

    public UpdateTransportOrderVO getUpdateTransportOrder() {
        return updateTransportOrder;
    }

    public void setUpdateTransportOrder(UpdateTransportOrderVO updateTransportOrder) {
        this.updateTransportOrder = updateTransportOrder;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "TOCommand{" + "createTransportOrder=" + createTransportOrder + ", updateTransportOrder=" + updateTransportOrder + ", type=" + type + '}';
    }

    public static final class Builder {
        private CreateTransportOrderVO createTransportOrder;
        private UpdateTransportOrderVO updateTransportOrder;
        private Type type;

        private Builder() {
        }

        public Builder withCreateTransportOrder(CreateTransportOrderVO val) {
            createTransportOrder = val;
            return this;
        }

        public Builder withUpdateTransportOrder(UpdateTransportOrderVO val) {
            updateTransportOrder = val;
            return this;
        }

        public Builder withType(Type val) {
            type = val;
            return this;
        }

        public TOCommand build() {
            return new TOCommand(this);
        }
    }
}

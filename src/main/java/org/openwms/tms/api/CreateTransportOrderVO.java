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

import org.openwms.tms.Message;

import java.io.Serializable;

/**
 * A CreateTransportOrderVO.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
public class CreateTransportOrderVO implements Serializable {

    private String pKey;
    private String barcode;
    private String priority;
    private Message problem;
    private String state;
    private String target;

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

    public Message getProblem() {
        return problem;
    }

    public void setProblem(Message problem) {
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
}

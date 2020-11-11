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
package org.openwms.tms.impl.state;

/**
 * A ExternalStarter is able to vote whether a {@code TransportOrder} shall be started or not. An implementation may work asynchronously and
 * may send out events to remote services that vote on starting or not. Finally this implementation would inject an instance of {@link Startable}
 * to trigger the actual start.
 *
 * @author Heiko Scherrer
 */
public interface ExternalStarter {

    /**
     * Request to start a {@code TransportOrder}.
     *
     * @param pKey The persistent key of the TransportOrder.
     */
    void request(String pKey);
}

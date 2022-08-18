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

import org.junit.jupiter.api.Test;
import org.openwms.TransportationTestBase;
import org.openwms.common.location.api.LocationVO;
import org.openwms.common.transport.api.TransportUnitVO;
import org.openwms.tms.api.CreateTransportOrderVO;
import org.openwms.tms.api.TMSApi;
import org.springframework.http.MediaType;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A PrioritizeTODocumentation.
 *
 * @author Heiko Scherrer
 */
class PrioritizeTODocumentation extends TransportationTestBase {

    @Test
    void prioritizeTOWithUnknownPriority() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);

        vo.setPriority("UNKNOWN");
        mockMvc.perform(
                patch(TMSApi.TRANSPORT_ORDERS +"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
                )
                .andExpect(status().isBadRequest())
                .andDo(document("to-patch-priority-with-unknown-priority"))
        ;

        // We do not expect a change of the priority
        mockMvc.perform(
                get(TMSApi.TRANSPORT_ORDERS + "/" + vo.getpKey()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("priority", is("HIGHEST")))
        ;
    }

    @Test
    void prioritizeTO() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        TransportUnitVO transportUnit = new TransportUnitVO(KNOWN);
        LocationVO location = new LocationVO(INIT_LOC_STRING);
        transportUnit.setActualLocation(location);
        LocationVO errorLocation = new LocationVO(ERR_LOC_STRING);
        errorLocation.setIncomingActive(true);
        errorLocation.setOutgoingActive(true);
        transportUnit.setTargetLocation(errorLocation);
        given(transportUnitApi.findTransportUnit(KNOWN)).willReturn(transportUnit);

        mockMvc.perform(
                get(TMSApi.TRANSPORT_ORDERS + "/" + vo.getpKey()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("priority", is("HIGHEST")))
        ;

        // test ...
        vo.setPriority(PriorityLevel.NORMAL.toString());
        mockMvc.perform(
                patch(TMSApi.TRANSPORT_ORDERS +"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
        )
                .andExpect(status().isNoContent())
                .andDo(document("to-patch-priority-ok"))
        ;

        mockMvc.perform(
                get(TMSApi.TRANSPORT_ORDERS + "/" + vo.getpKey()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("priority", is("NORMAL")))
        ;
    }
}

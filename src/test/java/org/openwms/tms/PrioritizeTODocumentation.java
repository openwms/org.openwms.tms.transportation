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
package org.openwms.tms;

import org.junit.Test;
import org.openwms.TransportationTestBase;
import org.openwms.common.location.api.LocationVO;
import org.openwms.common.transport.api.TransportUnitVO;
import org.openwms.tms.api.CreateTransportOrderVO;
import org.openwms.tms.api.TMS_API;
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
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
public class PrioritizeTODocumentation extends TransportationTestBase {

    public
    @Test
    void prioritizeTOWithNull() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);

        vo.setPriority(null);
        mockMvc.perform(
                patch(TMS_API.TRANSPORT_ORDERS +"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
                )
                .andExpect(status().isNoContent())
                .andDo(document("to-patch-priority-with-null"))
        ;

        mockMvc.perform(
                get(TMS_API.TRANSPORT_ORDERS + "/" + vo.getpKey()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("priority", is("NORMAL")))
        ;
    }

    public
    @Test
    void prioritizeTO() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        TransportUnitVO transportUnit = new TransportUnitVO();
        transportUnit.setBarcode(KNOWN);
        LocationVO location = new LocationVO();
        location.setLocationId(INIT_LOC_STRING);
        transportUnit.setActualLocation(location);
        transportUnit.setTarget(ERR_LOC_STRING);
        given(transportUnitApi.findTransportUnit(KNOWN, Boolean.FALSE)).willReturn(transportUnit);

        mockMvc.perform(
                get(TMS_API.TRANSPORT_ORDERS + "/" + vo.getpKey()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("priority", is("HIGHEST")))
        ;

        // test ...
        vo.setPriority(PriorityLevel.NORMAL.toString());
        mockMvc.perform(
                patch(TMS_API.TRANSPORT_ORDERS +"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
        )
                .andExpect(status().isNoContent())
                .andDo(document("to-patch-priority-ok"))
        ;

        mockMvc.perform(
                get(TMS_API.TRANSPORT_ORDERS + "/" + vo.getpKey()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("priority", is("NORMAL")))
        ;
    }
}

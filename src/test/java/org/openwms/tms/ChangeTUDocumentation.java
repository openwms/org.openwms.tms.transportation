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

import org.ameba.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.openwms.TransportationTestBase;
import org.openwms.common.location.api.LocationVO;
import org.openwms.common.transport.api.TransportUnitVO;
import org.openwms.tms.api.CreateTransportOrderVO;
import org.openwms.tms.api.TMSApi;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A ChangeTUDocumentation.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
public class ChangeTUDocumentation extends TransportationTestBase {

    public
    @Test
    void testTUChange() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        vo.setBarcode(KNOWN);
        TransportUnitVO transportUnit = new TransportUnitVO(KNOWN);
        LocationVO location = new LocationVO(INIT_LOC_STRING);
        transportUnit.setActualLocation(location);
        transportUnit.setTarget(ERR_LOC_STRING);
        given(transportUnitApi.findTransportUnit(KNOWN)).willReturn(transportUnit);

        // test ...
        mockMvc.perform(
                patch(TMSApi.TRANSPORT_ORDERS +"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
                )
                .andExpect(status().isNoContent())
                .andDo(document("to-patch-tu-change"))
        ;
    }

    public
    @Test
    void testTUChangeUnknownTU() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        vo.setBarcode(UNKNOWN);
        given(transportUnitApi.updateTU(any(), any())).willThrow(new NotFoundException("", "COMMON.BARCODE_NOT_FOUND", UNKNOWN));

        // test ...
        MvcResult res = mockMvc.perform(
                patch(TMSApi.TRANSPORT_ORDERS +"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
                )
                .andExpect(status().isNotFound())
                .andDo(document("to-patch-tu-unknown"))
                .andReturn()
        ;
        assertThat(res.getResponse().getContentAsString().contains("COMMON.BARCODE_NOT_FOUND")).isTrue();
    }

    public
    @Test
    void testTUChangeTUWithNUll() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        vo.setBarcode(null);
// TODO [openwms]: 10/08/16
        // test ...
        mockMvc.perform(
                patch(TMSApi.TRANSPORT_ORDERS +"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
        )
                .andExpect(status().isNoContent())
                .andDo(document("to-patch-tu-null"))
        ;
    }
}

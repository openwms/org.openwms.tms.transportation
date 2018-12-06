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
import org.junit.Test;
import org.openwms.TransportationTestBase;
import org.openwms.common.location.api.LocationVO;
import org.openwms.tms.api.CreateTransportOrderVO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A CreateTODocumentation is a system test to test the public API of the component. It is marked as {@link Transactional} and to be
 * roll-backed ({@link Rollback}) after each test run. The reason for this is to open the transaction bracket around the controller to
 * rollback it afterwards.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
public class CreateTODocumentation extends TransportationTestBase {

    public
    @Test
    void testCreateTO() throws Exception {
        MvcResult res = postTOAndValidate(createTO(), "to-create");
        assertThat(res.getResponse().getHeaderValue(HttpHeaders.LOCATION)).isNotNull();
    }

    public
    @Test
    void testCreateTOAndGet() throws Exception {
        CreateTransportOrderVO vo = createTO();
        MvcResult res = postTOAndValidate(vo, NOTLOGGED);

        String toLocation = (String) res.getResponse().getHeaderValue(HttpHeaders.LOCATION);
        mockMvc.perform(get(toLocation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("state", is(TransportOrderState.STARTED.toString())))
                .andExpect(jsonPath("sourceLocation", is(INIT_LOC_STRING)))
                .andExpect(jsonPath("targetLocation", is(ERR_LOC_STRING)))
                .andDo(document("to-create-and-get"))
        ;
    }

    public
    @Test
    void testCreateTOUnknownTU() throws Exception {
        CreateTransportOrderVO vo = createTO();
        vo.setBarcode("UNKNOWN");
        given(transportUnitApi.findTransportUnit(vo.getBarcode())).willThrow(new NotFoundException());

        mockMvc.perform(post(TMSConstants.ROOT_ENTITIES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vo)))
                .andExpect(status().isNotFound())
                .andDo(document("to-create-uk-tu"))
        ;
    }

    public
    @Test
    void testCreateTOUnknownTarget() throws Exception {
        CreateTransportOrderVO vo = createTO();
        vo.setTarget("UNKNOWN");
        given(locationApi.findLocationByCoordinate(vo.getTarget())).willReturn(null);
        given(locationGroupApi.findByName(vo.getTarget())).willReturn(null);

        mockMvc.perform(post(TMSConstants.ROOT_ENTITIES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vo)))
                .andExpect(status().isNotFound())
                .andReturn()
        ;
    }

    public
    @Test
    void testCreateTOTargetNotAvailable() throws Exception {
        CreateTransportOrderVO vo = createTO();
        vo.setTarget(ERR_LOC_STRING);
        LocationVO loc = new LocationVO();
        loc.setLocationId(ERR_LOC_STRING);
        loc.setIncomingActive(false);
        given(locationApi.findLocationByCoordinate(vo.getTarget())).willReturn(loc);

        MvcResult res = mockMvc.perform(post(TMSConstants.ROOT_ENTITIES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vo)))
                .andExpect(status().isCreated())
                .andReturn();

        String toLocation = (String) res.getResponse().getHeaderValue(HttpHeaders.LOCATION);
        mockMvc.perform(get(toLocation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("state", is(TransportOrderState.INITIALIZED.toString())))
                .andExpect(jsonPath("sourceLocation", is(INIT_LOC_STRING)))
                .andExpect(jsonPath("targetLocation", is(ERR_LOC_STRING)))
                .andDo(document("to-create-and-get-target-na"))
        ;
    }

    public
    @Test
    void testCreateTOUnknownPriority() throws Exception {
        CreateTransportOrderVO vo = createTO();
        vo.setPriority("UNKNOWN");

        mockMvc.perform(post(TMSConstants.ROOT_ENTITIES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vo)))
                .andExpect(status().isBadRequest())
                .andReturn()
        ;
    }
}

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

import org.ameba.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.openwms.TransportationTestBase;
import org.openwms.common.location.api.LocationVO;
import org.openwms.common.transport.api.TransportUnitVO;
import org.openwms.core.SpringProfiles;
import org.openwms.tms.api.TMSApi;
import org.openwms.tms.impl.state.ExternalStarter;
import org.openwms.tms.impl.state.Startable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
 * @author Heiko Scherrer
 */
class CreateTODocumentation extends TransportationTestBase {

    @Configuration
    static class TestConfig {

        @Profile(SpringProfiles.ASYNCHRONOUS_PROFILE)
        @Primary
        @Bean
        ExternalStarter DefaultStartListener(Startable startable) {
            return new TestExternalStarter(startable);
        }
    }

    @Test
    void testCreateTO() throws Exception {
        var res = postTOAndValidate(createTO(), "to-create");
        assertThat(res.getResponse().getHeaderValue(HttpHeaders.LOCATION)).isNotNull();
    }

    @Test
    void testCreateTOSimple() throws Exception {
        var actualLocation = new LocationVO(INIT_LOC_STRING);
        actualLocation.setIncomingActive(true);
        actualLocation.setOutgoingActive(true);
        var errorLocation = new LocationVO(ERR_LOC_STRING);
        errorLocation.setIncomingActive(true);
        errorLocation.setOutgoingActive(true);
        var tu = new TransportUnitVO(BC_4711);
        tu.setActualLocation(actualLocation);
        tu.setTargetLocation(errorLocation);

        given(transportUnitApi.findTransportUnit(BC_4711)).willReturn(tu);
        given(locationApi.findById(ERR_LOC_STRING)).willReturn(Optional.of(errorLocation));
        given(locationGroupApi.findByName(ERR_LOC_STRING)).willReturn(Optional.empty());

        var res = mockMvc.perform(post(TMSApi.TRANSPORT_ORDERS)
                .param("barcode", BC_4711)
                .param("target", ERR_LOC_STRING))
                .andExpect(status().isCreated())
                .andDo(document("to-create-simple"))
                .andReturn();
        assertThat(res.getResponse().getHeaderValue(HttpHeaders.LOCATION)).isNotNull();
    }

    @Test
    void testCreateTOAndGet() throws Exception {
        var vo = createTO();
        var res = postTOAndValidate(vo, NOTLOGGED);

        var toLocation = (String) res.getResponse().getHeaderValue(HttpHeaders.LOCATION);
        mockMvc.perform(get(toLocation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("state", is(TransportOrderState.STARTED.toString())))
                .andExpect(jsonPath("sourceLocation", is(INIT_LOC_STRING)))
                .andExpect(jsonPath("targetLocation", is(ERR_LOC_STRING)))
                .andExpect(jsonPath("transportUnitBK", is(BC_4711)))
                .andDo(document("to-create-and-get"))
        ;
    }

    @Test
    void testCreateTOUnknownTU() throws Exception {
        var vo = createTO();
        vo.setBarcode("UNKNOWN");
        given(transportUnitApi.findTransportUnit(vo.getBarcode())).willThrow(new NotFoundException());

        mockMvc.perform(post(TMSApi.TRANSPORT_ORDERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vo)))
                .andExpect(status().isNotFound())
                .andDo(document("to-create-uk-tu"))
        ;
    }

    @Test
    void testCreateTOUnknownTarget() throws Exception {
        var vo = createTO();
        vo.setTarget("UNKNOWN");
        given(locationApi.findById(vo.getTarget())).willReturn(Optional.empty());
        given(locationGroupApi.findByName(vo.getTarget())).willReturn(Optional.empty());

        mockMvc.perform(post(TMSApi.TRANSPORT_ORDERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vo)))
                .andExpect(status().isNotFound())
                .andReturn()
        ;
    }

    @Test
    void testCreateTOTargetNotAvailable() throws Exception {
        var vo = createTO();
        vo.setTarget(ERR_LOC_STRING);
        var loc = new LocationVO(ERR_LOC_STRING);
        loc.setIncomingActive(false);
        given(locationApi.findById(vo.getTarget())).willReturn(Optional.of(loc));

        var res = mockMvc.perform(post(TMSApi.TRANSPORT_ORDERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vo)))
                .andExpect(status().isCreated())
                .andReturn();

        var toLocation = (String) res.getResponse().getHeaderValue(HttpHeaders.LOCATION);
        mockMvc.perform(get(toLocation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("state", is(TransportOrderState.INITIALIZED.toString())))
                .andExpect(jsonPath("sourceLocation", is(INIT_LOC_STRING)))
                .andExpect(jsonPath("targetLocation", is(ERR_LOC_STRING)))
                .andExpect(jsonPath("transportUnitBK", is(BC_4711)))
                .andDo(document("to-create-and-get-target-na"))
        ;
    }

    @Test
    void testCreateTOUnknownPriority() throws Exception {
        var vo = createTO();
        vo.setPriority("UNKNOWN");

        mockMvc.perform(post(TMSApi.TRANSPORT_ORDERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vo)))
                .andExpect(status().isBadRequest())
                .andReturn()
        ;
    }
}

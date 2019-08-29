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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openwms.TransportationTestBase;
import org.openwms.common.location.api.LocationVO;
import org.openwms.common.transport.api.TransportUnitVO;
import org.openwms.tms.api.CreateTransportOrderVO;
import org.openwms.tms.api.TMSApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A ChangeStateDocumentation.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
class ChangeStateDocumentation extends TransportationTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeStateDocumentation.class);

    @Test void turnBackState() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        vo.setState(TransportOrderState.INITIALIZED.toString());
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("messageKey", is(TMSMessageCodes.TO_STATE_CHANGE_BACKWARDS_NOT_ALLOWED)))
                .andDo(document("to-patch-state-change-back"))
        ;
    }

    /* ----------------- INITIALIZED -------------------*/
    @Disabled("Test runs on OSX and Jenkins@Linux but not on TravisCI. Needs further investigation")
    @Test
    void createAnNewOneWhenOneIsAlreadyStarted() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        // create a second one that shall wait in INITIALIZED
        CreateTransportOrderVO vo2 = createTO();
        postTOAndValidate(vo2, NOTLOGGED);
        vo2.setState(TransportOrderState.STARTED.toString());
        TransportUnitVO transportUnit = new TransportUnitVO(KNOWN);
        LocationVO location = new LocationVO(INIT_LOC_STRING);
        transportUnit.setActualLocation(location);
        transportUnit.setTarget(ERR_LOC_STRING);
        given(transportUnitApi.findTransportUnit(KNOWN)).willReturn(transportUnit);

        LOGGER.debug("Calling API with:" + vo2);
        // test ...
        mockMvc.perform(
                patch(TMSApi.TRANSPORT_ORDERS +"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo2))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("messageKey", is(TMSMessageCodes.START_TO_NOT_ALLOWED_ALREADY_STARTED_ONE)))
                .andDo(document("to-patch-state-change-start-no-allowed-one-exists"))
        ;
    }

    @Test
    void cancellingAnInitializedOne() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        CreateTransportOrderVO vo2 = createTO();
        postTOAndValidate(vo2, NOTLOGGED);
        vo2.setState(TransportOrderState.CANCELED.toString());
        TransportUnitVO transportUnit = new TransportUnitVO(KNOWN);
        LocationVO location = new LocationVO(INIT_LOC_STRING);
        transportUnit.setActualLocation(location);
        transportUnit.setTarget(ERR_LOC_STRING);
        given(transportUnitApi.findTransportUnit(KNOWN)).willReturn(transportUnit);

        // test ...
        mockMvc.perform(
                patch(TMSApi.TRANSPORT_ORDERS +"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo2))
        )
                .andExpect(status().isNoContent())
                .andDo(document("to-patch-state-change-start-no-allowed-one-exists"))
        ;
    }

    @Test
    void settingAnInitializedOneOnFailure() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        CreateTransportOrderVO vo2 = createTO();
        postTOAndValidate(vo2, NOTLOGGED);
        vo2.setState(TransportOrderState.ONFAILURE.toString());
        TransportUnitVO transportUnit = new TransportUnitVO(KNOWN);
        LocationVO location = new LocationVO(INIT_LOC_STRING);
        transportUnit.setActualLocation(location);
        transportUnit.setTarget(ERR_LOC_STRING);

        given(transportUnitApi.findTransportUnit(KNOWN)).willReturn(transportUnit);

        // test ...
        mockMvc.perform(
                patch(TMSApi.TRANSPORT_ORDERS +"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo2))
        )
                .andExpect(status().isNoContent())
                .andDo(document("to-patch-state-initialize-to-failure"))
        ;
    }

    @Test
    void finishingAnInitializedOne() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        CreateTransportOrderVO vo2 = createTO();
        postTOAndValidate(vo2, NOTLOGGED);
        vo2.setState(TransportOrderState.FINISHED.toString());
        TransportUnitVO transportUnit = new TransportUnitVO(KNOWN);
        LocationVO location = new LocationVO(INIT_LOC_STRING);
        transportUnit.setActualLocation(location);
        transportUnit.setTarget(ERR_LOC_STRING);
        given(transportUnitApi.findTransportUnit(KNOWN)).willReturn(transportUnit);

        // test ...
        mockMvc.perform(
                patch(TMSApi.TRANSPORT_ORDERS +"/"+vo2.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo2))
        )
                .andExpect(status().isBadRequest())
                .andDo(document("to-patch-state-finish-an-initialized"))
        ;
    }

    /* ----------------- STARTED -------------------*/
    @Test
    void startingAnStartedOne() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        vo.setState(TransportOrderState.STARTED.toString());
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
                .andDo(document("to-patch-state-change"))
        ;
    }

    @Test
    void cancellingAnStartedOne() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        vo.setState(TransportOrderState.CANCELED.toString());
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
                .andDo(document("to-patch-state-cancel-a-started"))
        ;
    }

    @Test
    void settingAnStartedOneOnFailure() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        vo.setState(TransportOrderState.ONFAILURE.toString());
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
                .andDo(document("to-patch-state-onfailure-a-started"))
        ;
    }

    @Test
    void finishingAnStartedOne() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        vo.setState(TransportOrderState.FINISHED.toString());
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
                .andDo(document("to-patch-state-finish-a-started"))
        ;
    }

    /* ----------------- FINISHED -------------------*/
    @Test
    void changingAnFinishedOne() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        TransportUnitVO transportUnit = new TransportUnitVO(KNOWN);
        LocationVO location = new LocationVO(INIT_LOC_STRING);
        transportUnit.setActualLocation(location);
        transportUnit.setTarget(ERR_LOC_STRING);
        given(transportUnitApi.findTransportUnit(KNOWN)).willReturn(transportUnit);
        vo.setState(TransportOrderState.FINISHED.toString());
        mockMvc.perform(
                patch(TMSApi.TRANSPORT_ORDERS +"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
        )
                .andExpect(status().isNoContent())
        ;

        // test ...
        vo.setState(TransportOrderState.CANCELED.toString());
        mockMvc.perform(
                patch(TMSApi.TRANSPORT_ORDERS +"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
        )
                .andExpect(status().isBadRequest())
                .andDo(document("to-patch-state-change-a-finished"))
        ;
    }

    /* ----------------- ONFAILURE -------------------*/
    @Test
    void changingAnOnFailureOne() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        TransportUnitVO transportUnit = new TransportUnitVO(KNOWN);
        LocationVO location = new LocationVO(INIT_LOC_STRING);
        transportUnit.setActualLocation(location);
        transportUnit.setTarget(ERR_LOC_STRING);
        given(transportUnitApi.findTransportUnit(KNOWN)).willReturn(transportUnit);
        vo.setState(TransportOrderState.ONFAILURE.toString());
        mockMvc.perform(
                patch(TMSApi.TRANSPORT_ORDERS +"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
        )
                .andExpect(status().isNoContent())
        ;

        // test ...
        vo.setState(TransportOrderState.CANCELED.toString());
        mockMvc.perform(
                patch(TMSApi.TRANSPORT_ORDERS +"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
        )
                .andExpect(status().isBadRequest())
                .andDo(document("to-patch-state-change-an-onfailure"))
        ;
    }

    /* ----------------- CANCELED -------------------*/
    @Test
    void changingAnCanceledOne() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        TransportUnitVO transportUnit = new TransportUnitVO(KNOWN);
        LocationVO location = new LocationVO(INIT_LOC_STRING);
        transportUnit.setActualLocation(location);
        transportUnit.setTarget(ERR_LOC_STRING);
        given(transportUnitApi.findTransportUnit(KNOWN)).willReturn(transportUnit);
        vo.setState(TransportOrderState.CANCELED.toString());
        mockMvc.perform(
                patch(TMSApi.TRANSPORT_ORDERS +"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
        )
                .andExpect(status().isNoContent())
        ;

        // test ...
        vo.setState(TransportOrderState.ONFAILURE.toString());
        mockMvc.perform(
                patch(TMSApi.TRANSPORT_ORDERS +"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
        )
                .andExpect(status().isBadRequest())
                .andDo(document("to-patch-state-change-a-canceled"))
        ;
    }
}

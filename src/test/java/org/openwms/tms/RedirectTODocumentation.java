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
import org.openwms.tms.api.CreateTransportOrderVO;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A RedirectTODocumentation.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
public class RedirectTODocumentation extends TransportationTestBase {

    public
    @Test
    void testRedirectToUnknownLocationGroupButLoc() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        vo.setTarget(UNKNOWN);
        given(locationGroupApi.findByName(UNKNOWN)).willReturn(null);
        given(locationApi.findLocationByCoordinate(UNKNOWN)).willReturn(INIT_LOC);

        // test ...
        sendPatch(vo, status().isNoContent(), "to-patch-target-unknown-loc");
    }

    public
    @Test
    void testRedirectToUnknownLocationButLocGroup() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        vo.setTarget(UNKNOWN);
        given(locationGroupApi.findByName(UNKNOWN)).willReturn(ERR_LOCGRB);
        given(locationApi.findLocationByCoordinate(UNKNOWN)).willReturn(null);

        // test ...
        sendPatch(vo, status().isNoContent(), "to-patch-target-unknown-locgb");
    }

    public
    @Test
    void testRedirectToUnknownTargets() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        vo.setTarget(UNKNOWN);
        given(locationGroupApi.findByName(UNKNOWN)).willReturn(null);
        given(locationApi.findLocationByCoordinate(UNKNOWN)).willReturn(null);

        // test ...
        sendPatch(vo, status().isConflict(), "to-patch-target-unknown");
    }

    public
    @Test
    void testRedirectToBlockedLocation() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        vo.setTarget(INIT_LOC_STRING);
        given(locationGroupApi.findByName(INIT_LOC_STRING)).willReturn(null);
        INIT_LOC.setIncomingActive(false);
        given(locationApi.findLocationByCoordinate(INIT_LOC_STRING)).willReturn(INIT_LOC);

        // test ...
        sendPatch(vo, status().isConflict(), "to-patch-target-blocked-loc");
    }

    public
    @Test
    void testRedirectToBlockedLocationGroup() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        vo.setTarget(INIT_LOCGB_STRING);
        INIT_LOCGRB.setIncomingActive(false);
        given(locationGroupApi.findByName(INIT_LOCGB_STRING)).willReturn(INIT_LOCGRB);
        given(locationApi.findLocationByCoordinate(INIT_LOCGB_STRING)).willReturn(null);

        // test ...
        sendPatch(vo, status().isConflict(), "to-patch-target-blocked-locgrp");
    }

    private void sendPatch(CreateTransportOrderVO vo, ResultMatcher rm, String output) throws Exception {
        // test ...
        mockMvc.perform(
            patch(TMSConstants.ROOT_ENTITIES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vo))
            )
            .andExpect(rm)
            .andDo(document(output))
        ;
    }
}

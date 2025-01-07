/*
 * Copyright 2005-2025 the original author or authors.
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
package org.openwms;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.openwms.common.location.api.LocationApi;
import org.openwms.common.location.api.LocationGroupApi;
import org.openwms.common.location.api.LocationGroupState;
import org.openwms.common.location.api.LocationGroupVO;
import org.openwms.common.location.api.LocationVO;
import org.openwms.common.transport.api.TransportUnitApi;
import org.openwms.common.transport.api.TransportUnitVO;
import org.openwms.tms.PriorityLevel;
import org.openwms.tms.TMSApplicationTest;
import org.openwms.tms.api.CreateTransportOrderVO;
import org.openwms.tms.api.TMSApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A TransportationTestBase.
 *
 * @author Heiko Scherrer
 */
@TMSApplicationTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class TransportationTestBase {

    @Autowired
    protected ObjectMapper objectMapper;
    protected MockMvc mockMvc;
    @MockitoBean
    protected LocationApi locationApi;
    @MockitoBean
    protected LocationGroupApi locationGroupApi;
    @MockitoBean
    protected TransportUnitApi transportUnitApi;
    public static final String NOTLOGGED = "--";
    public static final String INIT_LOC_STRING = "INIT/0000/0000/0000/0000";
    protected LocationVO INIT_LOC;

    public static final String ERR_LOC_STRING = "ERR_/0000/0000/0000/0000";
    protected LocationVO ERR_LOC;

    public static final String INIT_LOCGB_STRING = "Picking";
    protected LocationGroupVO INIT_LOCGRB;

    public static final String ERR_LOCGB_STRING = "Error handling";
    protected LocationGroupVO ERR_LOCGRB;

    public static final String KNOWN = "KNOWN";
    public static final String UNKNOWN = "UNKNOWN";
    public static final String BC_4711 = "4711";

    /**
     * Do something before each test method.
     */
    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation, WebApplicationContext context) {
        INIT_LOC = new LocationVO(INIT_LOC_STRING);
        ERR_LOC = new LocationVO(ERR_LOC_STRING);
        INIT_LOCGRB = new LocationGroupVO(INIT_LOCGB_STRING, LocationGroupState.AVAILABLE, LocationGroupState.AVAILABLE);
        ERR_LOCGRB = new LocationGroupVO(ERR_LOCGB_STRING, LocationGroupState.AVAILABLE, LocationGroupState.AVAILABLE);
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @AfterEach
    public void reset_mocks() {
        Mockito.reset(locationApi, locationGroupApi, transportUnitApi);
    }

    protected CreateTransportOrderVO createTO() {
        var builder = CreateTransportOrderVO
                .newBuilder()
                .withPriority(PriorityLevel.HIGHEST.toString())
                .withBarcode(BC_4711)
                .withTarget(ERR_LOC_STRING)
                ;

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
        return builder.build();
    }

    protected MvcResult postTOAndValidate(CreateTransportOrderVO vo, String outputFile) throws Exception {
        MvcResult res;
        if (NOTLOGGED.equals(outputFile)) {
            res = mockMvc.perform(post(TMSApi.TRANSPORT_ORDERS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(vo)))
                    .andExpect(status().isCreated())
                    .andReturn();
        } else {
            res = mockMvc.perform(post(TMSApi.TRANSPORT_ORDERS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(vo)))
                    .andExpect(status().isCreated())
                    .andDo(document(outputFile))
                    .andReturn();
        }

        //TimeUnit.SECONDS.sleep(1);
        var toLocation = (String) res.getResponse().getHeaderValue(HttpHeaders.LOCATION);
        vo.setpKey(toLocation.substring(toLocation.lastIndexOf("/") + 1));
        return res;
    }
}

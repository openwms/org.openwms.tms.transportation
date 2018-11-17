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
package org.openwms;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.openwms.common.CommonGateway;
import org.openwms.common.Location;
import org.openwms.common.LocationGroup;
import org.openwms.common.TransportUnit;
import org.openwms.tms.PriorityLevel;
import org.openwms.tms.TMSConstants;
import org.openwms.tms.api.CreateTransportOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A TransportationTestBase.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT)
@Transactional
public abstract class TransportationTestBase {

    @Autowired
    @Qualifier(TMSConstants.BEAN_NAME_OBJECTMAPPER)
    protected ObjectMapper objectMapper;
    protected MockMvc mockMvc;
    @Autowired
    protected WebApplicationContext context;
    @MockBean
    protected CommonGateway commonGateway;
    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");
    public static final String NOTLOGGED = "--";
    public static final String INIT_LOC_STRING = "INIT/0000/0000/0000/0000";
    protected Location INIT_LOC;

    public static final String ERR_LOC_STRING = "ERR_/0000/0000/0000/0000";
    protected Location ERR_LOC;

    public static final String INIT_LOCGB_STRING = "Picking";
    protected LocationGroup INIT_LOCGRB;

    public static final String ERR_LOCGB_STRING = "Error handling";
    protected LocationGroup ERR_LOCGRB;

    public static final String KNOWN = "KNOWN";
    public static final String UNKNOWN = "UNKNOWN";
    public static final String BC_4711 = "4711";

    /**
     * Do something before each test method.
     *
     * @throws Exception Any error
     */
    @Before
    public void setUp() throws Exception {
        INIT_LOC = new Location(INIT_LOC_STRING);
        ERR_LOC = new Location(ERR_LOC_STRING);
        INIT_LOCGRB = new LocationGroup(INIT_LOCGB_STRING);
        ERR_LOCGRB = new LocationGroup(ERR_LOCGB_STRING);
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation).uris()
                        .withPort(8888))
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    protected CreateTransportOrderVO createTO() {
        CreateTransportOrderVO vo = new CreateTransportOrderVO();
        vo.setPriority(PriorityLevel.HIGHEST.toString());
        vo.setBarcode(BC_4711);
        vo.setTarget(ERR_LOC_STRING);

        Location actualLocation = new Location(INIT_LOC_STRING);
        Location errorLocation = new Location(ERR_LOC_STRING);
        TransportUnit tu = new TransportUnit(vo.getBarcode(), actualLocation, vo.getTarget());

        given(commonGateway.getTransportUnit(vo.getBarcode())).willReturn(Optional.of(tu));
        given(commonGateway.getLocation(vo.getTarget())).willReturn(Optional.of(errorLocation));
        given(commonGateway.getLocationGroup(vo.getTarget())).willReturn(Optional.empty());
        return vo;
    }

    protected MvcResult postTOAndValidate(CreateTransportOrderVO vo, String outputFile) throws Exception {
        MvcResult res;
        if (NOTLOGGED.equals(outputFile)) {
            res = mockMvc.perform(post(TMSConstants.ROOT_ENTITIES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(vo)))
                    .andExpect(status().isCreated())
                    .andReturn();
        } else {
            res = mockMvc.perform(post(TMSConstants.ROOT_ENTITIES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(vo)))
                    .andExpect(status().isCreated())
                    .andDo(document(outputFile))
                    .andReturn();
        }

        String toLocation = (String) res.getResponse().getHeaderValue(HttpHeaders.LOCATION);
        toLocation = toLocation.substring(0, toLocation.length() - 1);
        vo.setpKey(toLocation.substring(toLocation.lastIndexOf("/") + 1));
        return res;
    }
}

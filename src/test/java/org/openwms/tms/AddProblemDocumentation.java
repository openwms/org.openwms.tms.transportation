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

import org.ameba.mapping.BeanMapper;
import org.junit.jupiter.api.Test;
import org.openwms.TransportationTestBase;
import org.openwms.tms.api.CreateTransportOrderVO;
import org.openwms.tms.api.MessageVO;
import org.openwms.tms.api.TMSApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A AddProblemDocumentation.
 *
 * @author Heiko Scherrer
 */
class AddProblemDocumentation extends TransportationTestBase {

    @Autowired
    private EntityManager em;
    @Autowired
    private BeanMapper mapper;

    @Test
    void testNullAsAddProblem() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        MvcResult res = postTOAndValidate(vo, NOTLOGGED);
        MessageVO msg = MessageVO.newBuilder().messageText("text").messageNo("77").build();
        vo.setProblem(msg);
        addProblem(vo);
        assertThat(mapper.map(readTransportOrder(vo.getpKey()).getProblem(), MessageVO.class)).isEqualTo(msg);

        // test ...
        vo.setProblem(null);
        mockMvc.perform(
                patch(TMSApi.TRANSPORT_ORDERS +"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
        )
                .andExpect(status().isNoContent())
                .andDo(document("to-patch-addproblem-null"))
        ;

        assertThat(mapper.map(readTransportOrder(vo.getpKey()).getProblem(), MessageVO.class)).isEqualTo(msg);
        assertThat(getProblemHistories()).isEmpty();
    }

    @Test
    void testAddProblem() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        MessageVO msg = MessageVO.newBuilder().messageText("text").messageNo("77").build();
        vo.setProblem(msg);

        // test ...
        mockMvc.perform(
                patch(TMSApi.TRANSPORT_ORDERS +"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
        )
                .andExpect(status().isNoContent())
                .andDo(document("to-patch-addproblem"))
        ;
        assertThat(mapper.map(readTransportOrder(vo.getpKey()).getProblem(), MessageVO.class)).isEqualTo(msg);
        assertThat(getProblemHistories()).isEmpty();
    }

    @Test
    void testAddSecondProblem() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        MessageVO msg = MessageVO.newBuilder().messageText("text").messageNo("77").build();
        vo.setProblem(msg);

        addProblem(vo);
        MessageVO msg2 = MessageVO.newBuilder().messageText("text2").messageNo("78").build();
        vo.setProblem(msg2);

        // test ...
        mockMvc.perform(
            patch(TMSApi.TRANSPORT_ORDERS +"/"+vo.getpKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(vo))
            )
            .andExpect(status().isNoContent())
            .andDo(document("to-patch-addsecondproblem"))
        ;
        assertThat(mapper.map(readTransportOrder(vo.getpKey()).getProblem(), MessageVO.class)).isEqualTo(msg2);
        List<ProblemHistory> problemHistories = getProblemHistories();
        assertThat(problemHistories).hasSize(1);
    }

    private List<ProblemHistory> getProblemHistories() {
        return em.createQuery("select ph from ProblemHistory ph", ProblemHistory.class).getResultList();
    }

    private void addProblem(CreateTransportOrderVO vo) throws Exception {
        mockMvc.perform(
                patch(TMSApi.TRANSPORT_ORDERS +"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
        )
                .andExpect(status().isNoContent())
        ;
    }

    private TransportOrder readTransportOrder(String pKey) {
        return em.createQuery("select to from TransportOrder to where to.pKey = :pkey", TransportOrder.class).setParameter("pkey", pKey).getSingleResult();
    }
}

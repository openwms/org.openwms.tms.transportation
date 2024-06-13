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

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.openwms.TransportationTestBase;
import org.openwms.tms.api.CreateTransportOrderVO;
import org.openwms.tms.api.MessageVO;
import org.openwms.tms.api.TMSApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

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
    private TransportOrderMapper mapper;

    @Test
    void testNullAsAddProblem() throws Exception {
        // setup ...
        var vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        var msg = createMessage("text", "77");
        vo.setProblem(msg);
        addProblem(vo);

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

        assertThat(mapper.convertToVO(readTransportOrder(vo.getpKey()).getProblem())).isEqualTo(msg);
        assertThat(getProblemHistories()).isEmpty();
    }

    @Test
    void testAddProblem() throws Exception {
        // setup ...
        var vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        var msg = createMessage("text", "77");
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
        var updatedTO = readTransportOrder(vo.getpKey());
        var msgVO = mapper.convertToVO(updatedTO.getProblem());
        assertThat(msgVO).isEqualTo(msg);
        assertThat(getProblemHistories()).isEmpty();
    }

    @Test
    void testAddSecondProblem() throws Exception {
        // setup ...
        var vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        var msg = createMessage("text", "77");
        vo.setProblem(msg);

        addProblem(vo);
        var msg2 = createMessage("text2", "78");
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
        assertThat(mapper.convertToVO(readTransportOrder(vo.getpKey()).getProblem())).isEqualTo(msg2);
        var problemHistories = getProblemHistories();
        assertThat(problemHistories).hasSize(1);
    }

    private MessageVO createMessage(String text, String number) throws Exception {
        var msg = MessageVO.newBuilder().messageText(text).messageNo(number).build();
        // Let the VO through the mapper to format the date fields...
        return objectMapper.readValue(objectMapper.writeValueAsString(msg), MessageVO.class);
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

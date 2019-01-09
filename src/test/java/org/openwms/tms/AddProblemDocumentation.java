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
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
public class AddProblemDocumentation extends TransportationTestBase {

    @Autowired
    private EntityManager em;

    public
    @Test
    void testNullAsAddProblem() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        MvcResult res = postTOAndValidate(vo, NOTLOGGED);
        Message msg = new Message.Builder().withMessage("text").withMessageNo("77").build();
        vo.setProblem(msg);
        addProblem(vo);
        assertThat(readTransportOrder(vo.getpKey()).getProblem()).isEqualTo(msg);

        // test ...
        vo.setProblem(null);
        mockMvc.perform(
                patch(TMSConstants.ROOT_ENTITIES+"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
        )
                .andExpect(status().isNoContent())
                .andDo(document("to-patch-addproblem-null"))
        ;

        assertThat(readTransportOrder(vo.getpKey()).getProblem()).isEqualTo(msg);
        assertThat(getProblemHistories()).hasSize(0);
    }

    public
    @Test
    void testAddProblem() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        Message msg = new Message.Builder().withMessage("text").withMessageNo("77").build();
        vo.setProblem(msg);

        // test ...
        mockMvc.perform(
                patch(TMSConstants.ROOT_ENTITIES+"/"+vo.getpKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo))
        )
                .andExpect(status().isNoContent())
                .andDo(document("to-patch-addproblem"))
        ;
        assertThat(readTransportOrder(vo.getpKey()).getProblem()).isEqualTo(msg);
        assertThat(getProblemHistories()).hasSize(0);
    }

    public
    @Test
    void testAddSecondProblem() throws Exception {
        // setup ...
        CreateTransportOrderVO vo = createTO();
        postTOAndValidate(vo, NOTLOGGED);
        Message msg = new Message.Builder().withMessage("text").withMessageNo("77").build();
        vo.setProblem(msg);

        addProblem(vo);
        Message msg2 = new Message.Builder().withMessage("text2").withMessageNo("78").build();
        vo.setProblem(msg2);

        // test ...
        mockMvc.perform(
            patch(TMSConstants.ROOT_ENTITIES+"/"+vo.getpKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(vo))
            )
            .andExpect(status().isNoContent())
            .andDo(document("to-patch-addsecondproblem"))
        ;
        assertThat(readTransportOrder(vo.getpKey()).getProblem()).isEqualTo(msg2);
        List<ProblemHistory> problemHistories = getProblemHistories();
        assertThat(problemHistories).hasSize(1);
        assertThat(problemHistories.get(0))
                .extracting("problem")
                .contains(msg);
    }

    private List<ProblemHistory> getProblemHistories() {
        return em.createQuery("select ph from ProblemHistory ph", ProblemHistory.class).getResultList();
    }

    private void addProblem(CreateTransportOrderVO vo) throws Exception {
        mockMvc.perform(
                patch(TMSConstants.ROOT_ENTITIES+"/"+vo.getpKey())
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

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
package org.openwms.tms;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openwms.tms.api.CreateTransportOrderVO;
import org.openwms.tms.api.MessageVO;
import org.openwms.tms.api.TransportOrderVO;
import org.openwms.tms.api.UpdateTransportOrderVO;
import org.openwms.tms.api.messages.TransportOrderMO;

import java.util.List;

/**
 * A TransportOrderMapper.
 *
 * @author Heiko Scherrer
 */
@Mapper
public interface TransportOrderMapper {

    List<TransportOrderVO> convertToVO(List<TransportOrder> eos);

    @Mapping(target = "id", source = "persistentKey")
    @Mapping(target = "transportUnitId", source = "transportUnitBK")
    TransportOrderVO convertToVO(TransportOrder eo);

    @Mapping(target = "pKey", source = "persistentKey")
    @Mapping(target = "state", source = "state")
    @Mapping(target = "sourceLocation", source = "sourceLocation")
    @Mapping(target = "targetLocation", source = "targetLocation")
    @Mapping(target = "targetLocationGroup", source = "targetLocationGroup")
    @Mapping(target = "transportUnitBK", source = "transportUnitBK")
    TransportOrderMO convertToMO(TransportOrder eo);

    @Mapping(target = "persistentKey", source = "pKey")
    @Mapping(target = "transportUnitBK", source = "barcode")
    @Mapping(target = "targetLocation", source = "target")
    @Mapping(target = "targetLocationGroup", source = "targetLocationGroup")
    TransportOrder convertToEO(CreateTransportOrderVO vo);

    @Mapping(target = "persistentKey", source = "pKey")
    @Mapping(target = "transportUnitBK", source = "barcode")
    @Mapping(target = "state", source = "state", defaultExpression = "java( null )")
    TransportOrder convertToEO(UpdateTransportOrderVO vo);

    Message convertToEO(MessageVO vo);

    MessageVO convertToVO(Message eo);
}

/**
 * Copyright 2014-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package webase.mq.consumer.frontinterface;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webase.mq.consumer.base.code.ConstantCode;
import webase.mq.consumer.base.entity.BasePageResponse;
import webase.mq.consumer.base.entity.BaseResponse;
import webase.mq.consumer.base.exception.MqException;
import webase.mq.consumer.base.properties.ConstantProperties;
import webase.mq.consumer.frontinterface.entity.FrontInfo;

/**
 * @author marsli
 */
@Log4j2
@RestController
@RequestMapping("mq")
public class FrontController {

    @Autowired
    FrontInterfaceService frontService;
    @Autowired
    ConstantProperties constantProperties;

    /**
     * qurey front mq message.
     */
    @GetMapping(value = "/message/{frontId}")
    public BaseResponse getMessage(
            @PathVariable Integer frontId) throws MqException {
        // db get ip port by frontId
        FrontInfo frontInfo = new FrontInfo(constantProperties.getMqServer());
        Object res = frontService.getMqMessage(frontInfo.getFrontIp(), frontInfo.getFrontPort());
        return new BaseResponse(ConstantCode.SUCCESS, res);
    }

    /**
     * TEST front mq message.
     */
    @GetMapping(value = "/test/{groupId}")
    public BaseResponse testFront(
            @PathVariable Integer groupId) throws MqException {
        // db get ip port by frontId
        FrontInfo frontInfo = new FrontInfo(constantProperties.getMqServer());
        Object res = frontService.testGetFromFront(groupId, frontInfo.getFrontIp(), frontInfo.getFrontPort());
        return new BaseResponse(ConstantCode.SUCCESS, res);
    }
}

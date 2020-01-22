/**
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package webase.mq.consumer.frontinterface;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import webase.mq.consumer.base.exception.MqException;
import webase.mq.consumer.base.properties.ConstantProperties;


@Log4j2
@Service
public class FrontInterfaceService {

    @Autowired
    private FrontRestTools frontRestTools;

    @Qualifier(value = "genericRestTemplate")
    @Autowired
    private RestTemplate genericRestTemplate;

    @Autowired
    private ConstantProperties cproperties;

    /**
     * request from specific front.
     */
    private <T> T requestSpecificFront(int groupId, String frontIp, Integer frontPort,
                                       HttpMethod method, String uri, Object param, Class<T> clazz) {
        log.debug(
                "start requestSpecificFront. groupId:{} frontIp:{} frontPort:{} "
                        + "httpMethod:{} uri:{}",
                groupId, frontIp, frontPort, method.toString(), uri);

        uri = FrontRestTools.uriAddGroupId(groupId, uri);
        String url = String.format(cproperties.getFrontUrl(), frontIp, frontPort, uri);
        log.debug("requestSpecificFront. url:{}", url);

        try {
            HttpEntity entity = FrontRestTools.buildHttpEntity(param);// build entity
            ResponseEntity<T> response = genericRestTemplate.exchange(url, method, entity, clazz);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            JSONObject error = JSONObject.parseObject(e.getResponseBodyAsString());
            throw new MqException(error.getInteger("code"), error.getString("errorMessage"));
        }
    }

    /**
     * get from specific front.
     */
    private <T> T getFromSpecificFront(int groupId, String frontIp, Integer frontPort, String uri,
            Class<T> clazz) {
        log.debug("start getFromSpecificFront. groupId:{} frontIp:{} frontPort:{}  uri:{}", groupId,
                frontIp, frontPort.toString(), uri);
        String url = String.format(cproperties.getFrontUrl(), frontIp, frontPort, uri);
        log.debug("getFromSpecificFront. url:{}", url);
        return requestSpecificFront(groupId, frontIp, frontPort, HttpMethod.GET, uri, null, clazz);
    }

    /**
     * get queue message from specific front.
     */
    public Object getMqMessage(String frontIp, Integer frontPort) {
        Integer groupId = Integer.MAX_VALUE;
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.GET_QUEUE_MESSAGE, Object.class);
    }


    /**
     * get queue message from specific front.
     */
    public Object testGetFromFront(Integer groupId, String frontIp, Integer frontPort) {
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.TEST_URI_GROUP_PLIST, Object.class);
    }

}

/**
 * Copyright 2014-2019  the original author or authors.
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import webase.mq.consumer.base.code.ConstantCode;
import webase.mq.consumer.base.exception.MqException;
import webase.mq.consumer.base.properties.ConstantProperties;
import webase.mq.consumer.frontinterface.entity.FailInfo;
import webase.mq.consumer.frontinterface.entity.FrontInfo;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * about http request for WeBASE-Front.
 */
@Log4j2
@Service
public class FrontRestTools {

    public static final String GET_QUEUE_MESSAGE = "mq/nextDelivery";
    public static final String TEST_URI_GROUP_PLIST = "web3/groupList";

    //不需要在url中包含groupId的
    private static final List<String> URI_NOT_CONTAIN_GROUP_ID = Arrays
            .asList(GET_QUEUE_MESSAGE);


    @Qualifier(value = "genericRestTemplate")
    @Autowired
    private RestTemplate genericRestTemplate;

    @Autowired
    private ConstantProperties cproperties;

    private static Map<String, FailInfo> failRequestMap = new HashMap<>();


    /**
     * append groupId to uri.
     */
    public static String uriAddGroupId(Integer groupId, String uri) {
        if (groupId == null || StringUtils.isBlank(uri)) {
            return null;
        }

        final String tempUri = uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri;

        long count = URI_NOT_CONTAIN_GROUP_ID.stream().filter(u -> u.contains(tempUri)).count();
        if (count > 0) {
            return uri;
        }
        return groupId + "/" + uri;
    }

    /**
     * check url status.
     */
    private boolean isServiceSleep(String url, String methType) {
        //get failInfo
        String key = buildKey(url, methType);
        FailInfo failInfo = failRequestMap.get(key);

        //cehck server status
        if (failInfo == null) {
            return false;
        }
        int failCount = failInfo.getFailCount();
        Long subTime = Duration.between(failInfo.getLatestTime(), Instant.now()).toMillis();
        if (failCount > cproperties.getMaxRequestFail() && subTime < cproperties
            .getSleepWhenHttpMaxFail()) {
            return true;
        } else if (subTime > cproperties.getSleepWhenHttpMaxFail()) {
            //service is sleep
            deleteKeyOfMap(failRequestMap, key);
        }
        return false;

    }

    /**
     * set request fail times.
     */
    private void setFailCount(String url, String methodType) {
        //get failInfo
        String key = buildKey(url, methodType);
        FailInfo failInfo = failRequestMap.get(key);
        if (failInfo == null) {
            failInfo = new FailInfo();
            failInfo.setFailUrl(url);
        }

        //reset failInfo
        failInfo.setLatestTime(Instant.now());
        failInfo.setFailCount(failInfo.getFailCount() + 1);
        failRequestMap.put(key, failInfo);
        log.info("the latest failInfo:{}", JSON.toJSONString(failRequestMap));
    }


    /**
     * build key description: frontIp$frontPort example: 2651654951545$8081
     */
    private String buildKey(String url, String methodType) {
        return url.hashCode() + "$" + methodType;
    }


    /**
     * delete key of map
     */
    private static void deleteKeyOfMap(Map<String, FailInfo> map, String rkey) {
        log.info("start deleteKeyOfMap. rkey:{} map:{}", rkey, JSON.toJSONString(map));
        Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            if (rkey.equals(key)) {
                iter.remove();
            }
        }
        log.info("end deleteKeyOfMap. rkey:{} map:{}", rkey, JSON.toJSONString(map));
    }


    /**
     * build  url of front service.
     */
    private String buildFrontUrl(FrontInfo frontInfo, String uri, HttpMethod httpMethod) {
        log.info("====================frontInfo:{}",JSON.toJSONString(frontInfo));
        uri = uriAddGroupId(frontInfo.getGroupId(), uri);
        String url = String
            .format(cproperties.getFrontUrl(), frontInfo.getFrontIp(),
                    frontInfo.getFrontPort(), uri)
            .replaceAll(" ", "");

        if (isServiceSleep(url, httpMethod.toString())) {
            log.warn("front url[{}] is sleep,jump over", url);
        }
        return url;
    }

    /**
     * build httpEntity
     */
    public static HttpEntity buildHttpEntity(Object param) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        String paramStr = null;
        if (Objects.nonNull(param)) {
            paramStr = JSON.toJSONString(param);
        }
        HttpEntity requestEntity = new HttpEntity(paramStr, headers);
        return requestEntity;
    }

    /**
     * case restTemplate by uri.
     */
    private RestTemplate caseRestemplate(String uri) {
        if (StringUtils.isBlank(uri)) {
            return null;
        }
//        if (uri.contains(URI_CONTRACT_DEPLOY)) {
//            return deployRestTemplate;
//        }
        return genericRestTemplate;
    }


    /**
     * get from front for entity.
     */
    public <T> T getForEntity(Integer groupId, String uri, Class<T> clazz) {
        return restTemplateExchange(groupId, uri, HttpMethod.GET, null, clazz);
    }

    /**
     * post from front for entity.
     */
    public <T> T postForEntity(Integer groupId, String uri, Object params, Class<T> clazz) {
        return restTemplateExchange(groupId, uri, HttpMethod.POST, params, clazz);
    }

    /**
     * delete from front for entity.
     */
    public <T> T deleteForEntity(Integer groupId, String uri, Object params, Class<T> clazz) {
        return restTemplateExchange(groupId, uri, HttpMethod.DELETE, params, clazz);
    }

    /**
     * restTemplate exchange.
     */
    private <T> T restTemplateExchange(int groupId, String uri, HttpMethod method,
        Object param, Class<T> clazz) {
        FrontInfo frontInfo = new FrontInfo(cproperties.getFrontUrl());
        RestTemplate restTemplate = caseRestemplate(uri);

        String url = buildFrontUrl(frontInfo, uri, method);//build url
        try {
            HttpEntity entity = buildHttpEntity(param);// build entity
            if (null == restTemplate) {
                log.error("fail restTemplateExchange, rest is null. groupId:{} uri:{}", groupId,uri);
                throw new MqException(ConstantCode.SYSTEM_EXCEPTION);
            }
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, clazz);
            return response.getBody();
        } catch (ResourceAccessException ex) {
            log.warn("fail restTemplateExchange", ex);
            setFailCount(url, method.toString());
            if (isServiceSleep(url, method.toString())) {
                throw ex;
            }
            log.info("continue next front", ex);
        } catch (HttpStatusCodeException e) {
            JSONObject error = JSONObject.parseObject(e.getResponseBodyAsString());
            log.error("http request fail. error:{}", JSON.toJSONString(error));
            // TODO check nullpointer of 'error.getInteger("code")'
            throw new MqException(error.getInteger("code"),
                error.getString("errorMessage"));
        }

        return null;
    }
}
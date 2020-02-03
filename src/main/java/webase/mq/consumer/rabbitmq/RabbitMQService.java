///*
// * Copyright 2014-2019 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package webase.mq.consumer.rabbitmq;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.core.*;
//import org.springframework.amqp.rabbit.core.RabbitAdmin;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 初始化RabbitMQ实例
// * @author marsli
// */
//@Slf4j
//public class RabbitMQService {
//
//    @Autowired
//    private RabbitAdmin rabbitAdmin;
//
//    private Map<String,List<String>> distinctList(List<String> list){
//        Map<String,List<String>> map = new HashMap<>();
//        for (String string :list){
//            if(!string.contains(".")){
//                continue;
//            }
//            List<String> list1 = new ArrayList<>();
//            String key =string.split("\\.")[0];
//            if(map.keySet().contains(key)){
//                map.get(key).add(string);
//            }else{
//                list1.add(string);
//                map.put(key,list1);
//            }
//        }
//        return map;
//    }
//
//
//    private void binding(List<String> queueNames){
//        Map<String ,List<String>> map = distinctList(queueNames);
//        for (String string : map.keySet()){
//            FanoutExchange fanoutExchange = new FanoutExchange(string);
//            for(String string1 : map.get(string)){
//                Binding binding = BindingBuilder.bind(new Queue(string1)).to(fanoutExchange);
//                rabbitAdmin.declareQueue(new Queue(string1));
//                rabbitAdmin.declareExchange(fanoutExchange);
//                rabbitAdmin.declareBinding(binding);
//            }
//        }
//    }
//
//}

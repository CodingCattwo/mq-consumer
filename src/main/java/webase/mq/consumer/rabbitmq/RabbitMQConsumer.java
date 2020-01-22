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

package webase.mq.consumer.rabbitmq;

import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import webase.mq.consumer.rabbitmq.entity.BlockPushMessage;

/**
 * TODO 通过factory动态设置listener的队列名
 * @author marsli
 */
@Log4j2
@Component
public class RabbitMQConsumer {

    @Autowired
    RabbitTemplate rabbitTemplate;


    /**
     * RabbitHandler handler通过消息体类型区分，同时Listener要注解在类上
     * Listener注解注解在方法上则按队列来监听
     *
     * @param message
     */
//    @RabbitListener(queues = "block_exchange.block_queue_1")
//    public void receive(Message message) {
//        log.info("++++++++mq receive: {}, type:{}",
//                message.getBody(),  message.getMessageProperties().getHeaders());
//    }

    @RabbitListener(queues = "block_exchange.block_queue_1")
    public void receiveStr(String message) {
        log.info("++++++++mq receive: {}", message);

    }
}

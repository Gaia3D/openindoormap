package io.openindoormap.service.impl;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.domain.common.QueueMessage;
import io.openindoormap.service.AMQPPublishService;

/**
 * @author Cheon JeongDae
 *
 */
@Slf4j
@Service
public class AMQPPublishServiceImpl implements AMQPPublishService {

	@Autowired
	private PropertiesConfig propertiesConfig;
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@Transactional
	public void send(QueueMessage queueMessage) throws AmqpException {
		log.info("@@ Publish send message >>> {}", queueMessage);
//		rabbitTemplate.convertAndSend(propertiesConfig.getQueueName(), queueMessage);

//		Message message = null;
//		try {
//			message = MessageBuilder
//					.withBody(queueMessage.toString().getBytes("UTF-8"))
//					.setContentType(MessageProperties.CONTENT_TYPE_JSON)
//					.build();
//		} catch(UnsupportedEncodingException e) {
//			e.printStackTrace();
//			throw new AmqpException("QueueMessage GetBytes Exception");
//		}

		rabbitTemplate.convertAndSend(propertiesConfig.getExchange(), propertiesConfig.getQueueName(), queueMessage);
	}
}

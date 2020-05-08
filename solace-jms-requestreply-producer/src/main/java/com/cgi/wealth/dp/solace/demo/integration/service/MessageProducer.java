package com.cgi.wealth.dp.solace.demo.integration.service;

import com.solacesystems.jms.SupportedProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.util.UUID;

/**
 * Message producer.
 */
@Service
public class MessageProducer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(MessageProducer.class);

    private JmsTemplate jmsTemplate;
    private int numOfMessages = 1;

    @Value("${solace.jms.demo-send-request-topic}")
    private String sendRequestTopic;
    @Value("${solace.jms.demo-request-reply-queue}")
    private String requestReplyQueue;
    private Destination replyTo;

    /**
     * Instantiates a new Message producer.
     *
     * @param jmsTemplate the jms template
     */
    public MessageProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * Create a number of messages to one of the event topic roots directly
     * @param args
     */
    public void run(String... args) throws Exception {
        replyTo = createReplyTo();
        for(int i = 0; i < numOfMessages; i++) {
            String msg = String.format("Producer sent message %s", i);
            if (logger.isDebugEnabled()) {
                String output = String.format("============= %s", msg);
                logger.debug(output);
            }
            sendMsg(sendRequestTopic, msg);
        }
    }

    public void sendMsg(String destination, String msg) throws Exception {
        String correlationId = UUID.randomUUID().toString();
        this.jmsTemplate.convertAndSend(destination, msg, message -> {
            message.setJMSCorrelationID(correlationId);
            message.setJMSReplyTo(replyTo);
            return message;
        });

        Message reply = this.jmsTemplate.receive(replyTo);
        processReply(reply, correlationId);
    }

    private Destination createReplyTo() throws JMSException {
        Session session = this.jmsTemplate.getConnectionFactory().createConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
        return session.createQueue(requestReplyQueue);
    }

    private void processReply(Message reply, String correlationId) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(reply.toString());
        }
        if (reply == null) {
            throw new Exception("Failed to receive a reply");
        }
        if (reply.getJMSCorrelationID() == null) {
            throw new Exception(
                    "Received a reply message with no correlationID. This field is needed for a direct request.");
        }
        if (!reply.getJMSCorrelationID().replaceAll("ID:", "").equals(correlationId)) {
            throw new Exception("Received invalid correlationID in reply message.");
        }
        if (reply instanceof TextMessage) {
            System.out.printf("TextMessage response received: '%s'%n", ((TextMessage) reply).getText());
            if (!reply.getBooleanProperty(SupportedProperty.SOLACE_JMS_PROP_IS_REPLY_MESSAGE)) {
                System.out.println("Warning: Received a reply message without the isReplyMsg flag set.");
            }
        } else {
            System.out.println("Message response received.");
        }

    }
}

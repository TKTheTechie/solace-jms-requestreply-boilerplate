package com.cgi.wealth.dp.solace.demo.integration.handler;

import com.solacesystems.jms.SupportedProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.springframework.jms.annotation.JmsListener;

import javax.jms.*;
import java.util.Iterator;


/**
 * Message handler for Consumer.
 */
@Component
public class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);


    /**
     * On message.
     *
     * @param msg        the msg
     * @param session    the session
     * @throws JMSException the jms exception
     */
    /* Retrieve the name of the queue from the application.properties file
     * Modify the number of concurrencies to enable multi-threading
     * To consume messages from all threads, go to the Queue tab in Solace admin UI 
     * and change the access type of your queue to Non-exclusive
     */
    @JmsListener(destination = "${solace.jms.demo-queue-name}", containerFactory = "cFactory", selector = "",  concurrency = "1")
    public void onMessage(Message<String> msg, javax.jms.Message origMsg, Session session) throws JMSException {
        processMsg(msg);

        TextMessage replyMsg = session.createTextMessage();
        replyMsg.setJMSCorrelationID(origMsg.getJMSCorrelationID());
        replyMsg.setBooleanProperty(SupportedProperty.SOLACE_JMS_PROP_IS_REPLY_MESSAGE, Boolean.TRUE);

        String replyContent = "Received message " + origMsg.getJMSCorrelationID() + ". Sending the response.";
        replyMsg.setText(replyContent);

        // send response with a new producer every time when the listener receives a new message
        final MessageProducer producer = session.createProducer(origMsg.getJMSReplyTo());
        producer.send(replyMsg, DeliveryMode.NON_PERSISTENT, javax.jms.Message.DEFAULT_PRIORITY, javax.jms.Message.DEFAULT_TIME_TO_LIVE);

    }

    public void processMsg(Message<String> msg) {
        StringBuilder msgAsStr = new StringBuilder("============= Received \nHeaders:");
        MessageHeaders hdrs = msg.getHeaders();
        msgAsStr.append("\nUUID: "+hdrs.getId());
        msgAsStr.append("\nTimestamp: "+hdrs.getTimestamp());
        Iterator<String> keyIter = hdrs.keySet().iterator();
        while (keyIter.hasNext()) {
            String key = keyIter.next();
            msgAsStr.append("\n"+key+": "+hdrs.get(key));
        }
        msgAsStr.append("\nPayload: "+msg.getPayload());
        if (logger.isDebugEnabled()) {
            logger.debug(msgAsStr.toString());
        }
    }
}
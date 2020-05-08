package com.cgi.wealth.dp.solace.demo.web;

import com.cgi.wealth.dp.solace.demo.integration.service.MessageProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * The Message controller.
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {
    private MessageProducer messageProducer;
    @Value("${solace.jms.demo-send-request-topic}")
    private String sendRequestTopic;

    /**
     * Instantiates a new Message controller.
     *
     * @param messageProducer the message producer
     */
    public MessageController(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    /**
     * Create a message to be delivered to message broker to create a new transaction
     * Allow users to specify event topic properties dynamically, and append them to topic root
     *
     * @param msg        the msg
     */
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody String msg) throws Exception {
        messageProducer.sendMsg(sendRequestTopic, msg);
    }
}

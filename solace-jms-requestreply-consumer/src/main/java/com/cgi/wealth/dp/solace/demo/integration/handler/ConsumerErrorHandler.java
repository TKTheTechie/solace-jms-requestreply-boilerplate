package com.cgi.wealth.dp.solace.demo.integration.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * Custom Consumer error handler
 */
@Service
public class ConsumerErrorHandler implements ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerErrorHandler.class);

    @Override
    public void handleError(Throwable t) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        t.printStackTrace(ps);
        try {
            String output = os.toString("UTF8");
            String formattedOutput = String.format("============= Error processing message: %s%n%s", t.getMessage(), output);
            logger.error(formattedOutput);
        } catch (UnsupportedEncodingException e) {
            logger.error("============= Error processing message: unsupported encoding", e);
        }
    }
}
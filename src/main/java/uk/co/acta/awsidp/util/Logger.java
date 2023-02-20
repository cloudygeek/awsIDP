package uk.co.acta.awsidp.util;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

import java.text.SimpleDateFormat;

public class Logger {
    private static LambdaLogger logger;

    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");

    public static void setLogger(LambdaLogger incomingLogger) {
        logger = incomingLogger;
    }
    public static void log(String logLine) {

        if (logger != null) {
            logger.log(logLine);
        } else {
            System.out.println(formatter.format(new java.util.Date()) + " " + logLine);
        }
    }

    public static boolean isLamba() {
        return logger!=null;
    }
}

package com.davidcubesvk.yamlUpdater.core.utils;

/**
 * An exception used to describe an error, which happened during the parsing/updating process.
 */
public class ParseException extends Exception {

    /**
     * Initializes the exception with the given message.
     * @param message the message
     */
    public ParseException(String message) {
        super(message);
    }

    /**
     * Initializes the exception with the given message and cause.
     * @param message the message
     * @param cause the cause
     */
    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

}
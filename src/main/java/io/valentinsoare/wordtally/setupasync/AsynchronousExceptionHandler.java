package io.valentinsoare.wordtally.setupasync;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.valentinsoare.wordtally.exception.ErrorMessage;
import io.valentinsoare.wordtally.exception.Severity;
import io.valentinsoare.wordtally.outputformat.OutputFormat;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/***
 * Custom exception handler for handling uncaught exceptions in asynchronous operations.
 * Implements {@link AsyncUncaughtExceptionHandler} to provide a mechanism for dealing with exceptions
 * that occur during the execution of asynchronous methods.
 */
public class AsynchronousExceptionHandler implements AsyncUncaughtExceptionHandler {

    private final OutputFormat outputFormat;

    /**
     * Constructs an AsynchronousExceptionHandler with a specific output format.
     * This output format is used to format the exception messages before printing them.
     *
     * @param outputFormat The {@link OutputFormat} used for formatting exception messages.
     */
    public AsynchronousExceptionHandler(OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    /**
     * Handles uncaught exceptions thrown during asynchronous method execution.
     * Formats the exception details and prints them using the specified {@link OutputFormat}.
     *
     * @param ex The exception that was thrown.
     * @param method The method during which the exception was thrown.
     * @param params The parameters with which the method was called.
     */
    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        ErrorMessage msg = ErrorMessage.builder()
                .message(ex.getMessage())
                .methodName(method.getName())
                .threadName(Thread.currentThread().getName())
                .dateTime(LocalDateTime.now().toString())
                .severity(Severity.ERROR)
                .clazzName(method.getDeclaringClass().getName())
                .build();

        try {
            System.err.printf("%s %n", outputFormat.withJSONStyle().writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            System.err.printf("%s %n", e.getMessage());
        }
    }
}

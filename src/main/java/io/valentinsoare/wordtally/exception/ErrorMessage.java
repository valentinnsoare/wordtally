package io.valentinsoare.wordtally.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/***
 * This ErrorMessage page represents a template for error messages that appear in the application.
 * When there is an error/exception, an object is created from this specific class and then serialized
 * and printed in JSON format as a key-value pair in string format in the terminal.
 */

@Getter
@Setter
@Builder
public class ErrorMessage {
    private Severity severity;
    private String clazzName;
    private String methodName;
    private String threadName;
    private String message;
    private String dateTime;

    /**
     * Default constructor for ErrorMessage class.
     */
    public ErrorMessage() {}

    /**
     * Parameterized constructor for ErrorMessage class.
     *
     * @param severity   The severity level of the error.
     * @param clazzName  The name of the class where the error occurred.
     * @param methodName The name of the method where the error occurred.
     * @param threadName The name of the thread where the error occurred.
     * @param message    The error message.
     * @param dateTime   The date and time when the error occurred.
     */
    public ErrorMessage(Severity severity, String clazzName, String methodName,
                        String threadName, String message, String dateTime) {
        this.severity = severity;
        this.clazzName = clazzName;
        this.methodName = methodName;
        this.threadName = threadName;
        this.message = message;
        this.dateTime = dateTime;
    }

    /**
     * Overrides the default toString method.
     *
     * @return A string representation of the ErrorMessage object.
     */
    @Override
    public String toString() {
        return String.format("Severity: %s, ClazzName: %s, MethodName: %s, ThreadName: %s, Message: %s, DateTime: %s",
                severity, clazzName, methodName, threadName, message, dateTime);
    }
}

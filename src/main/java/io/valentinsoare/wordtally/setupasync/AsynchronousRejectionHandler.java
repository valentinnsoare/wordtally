package io.valentinsoare.wordtally.setupasync;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.valentinsoare.wordtally.exception.ErrorMessage;
import io.valentinsoare.wordtally.exception.Severity;
import io.valentinsoare.wordtally.outputformat.OutputFormat;

import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/***
 * Handles the rejection of tasks submitted to the asynchronous execution pool.
 * This class implements {@link RejectedExecutionHandler} to provide custom logic for handling task rejections,
 * such as logging or notifying stakeholders when a task cannot be accepted by the thread pool.
 */
public class AsynchronousRejectionHandler implements RejectedExecutionHandler {

    private final OutputFormat outputFormat;

    /**
     * Constructs an AsynchronousRejectionHandler with a specific output format for logging rejection messages.
     * The output format is used to format the rejection messages before printing them, allowing for consistent
     * and readable logs.
     *
     * @param outputFormat The {@link OutputFormat} used for formatting rejection messages.
     */
    public AsynchronousRejectionHandler(OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    /**
     * Handles the rejection of a task submitted to the thread pool.
     * This method is called when a task is rejected, and it logs the rejection details using the specified
     * {@link OutputFormat}. The log includes information such as the class name, thread name, method name,
     * date and time of rejection, and a custom message indicating that the task was rejected.
     *
     * @param r The runnable task that was rejected.
     * @param executor The {@link ThreadPoolExecutor} that rejected the task.
     */
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        ErrorMessage msg = ErrorMessage.builder()
                .severity(Severity.INFO)
                .clazzName(Executor.class.toString())
                .threadName(Thread.currentThread().getName())
                .methodName("Execute")
                .dateTime(LocalDateTime.now().toString())
                .message("Current task is rejected by the threadPool")
                .build();

        try {
            System.err.printf("%s %n", outputFormat.withJSONStyle().writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            System.err.printf("%s %n", e.getMessage());
        }
    }
}

package io.valentinsoare.wordtally.setupasync;

import io.valentinsoare.wordtally.outputformat.OutputFormat;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

/***
 * With this we defined the type of executor, and as you can see we have a singleton design pattern here, so we will create
 * an executor from the DynamicThreadPoolManager and then whenever we need we will use it.
 */

@EnableAsync
@Configuration
public class AsynchronousConfiguration implements AsyncConfigurer {
    private DynamicThreadPoolManager defaultDynamicThreadPoolManager;

    /**
     * Default constructor for {@link AsynchronousConfiguration}.
     * Initializes a new instance of the configuration class without any initial setup.
     */
    public AsynchronousConfiguration() {}

    /**
     * Provides the executor for asynchronous task execution.
     * This method ensures that a single {@link DynamicThreadPoolManager} instance is used across the application,
     * following the singleton design pattern.
     *
     * @return An instance of {@link Executor} that manages asynchronous task execution.
     */
    @Override
    public Executor getAsyncExecutor() {
        if (defaultDynamicThreadPoolManager == null) {
            this.defaultDynamicThreadPoolManager = DynamicThreadPoolManager.generateNewDynamicThreadPoolManager();
        }

        return defaultDynamicThreadPoolManager;
    }

    /**
     * Provides the exception handler for uncaught exceptions in asynchronous methods.
     * This method creates a new {@link AsynchronousExceptionHandler} instance, which uses {@link OutputFormat}
     * for formatting exception messages.
     *
     * @return An instance of {@link AsyncUncaughtExceptionHandler} for handling exceptions in asynchronous methods.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsynchronousExceptionHandler(new OutputFormat());
    }
}

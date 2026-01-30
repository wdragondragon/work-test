package org.example.worktest.flink.exception;

public class FlinkJobException extends RuntimeException {

    public enum ErrorType {
        JOB_NOT_FOUND,
        JOB_ALREADY_RUNNING,
        CONFIGURATION_ERROR,
        NETWORK_ERROR,
        SUBMISSION_ERROR,
        STOP_ERROR,
        SAVEPOINT_ERROR,
        VALIDATION_ERROR
    }

    private final ErrorType errorType;

    public FlinkJobException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public FlinkJobException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public static FlinkJobException jobNotFound(String jobId) {
        return new FlinkJobException(ErrorType.JOB_NOT_FOUND, 
            "Flink job not found: " + jobId);
    }

    public static FlinkJobException jobAlreadyRunning(String jobId) {
        return new FlinkJobException(ErrorType.JOB_ALREADY_RUNNING,
            "Flink job is already running: " + jobId);
    }

    public static FlinkJobException configurationError(String message, Throwable cause) {
        return new FlinkJobException(ErrorType.CONFIGURATION_ERROR,
            "Configuration error: " + message, cause);
    }

    public static FlinkJobException submissionError(String jobName, Throwable cause) {
        return new FlinkJobException(ErrorType.SUBMISSION_ERROR,
            "Failed to submit job: " + jobName, cause);
    }

    public static FlinkJobException stopError(String jobId, Throwable cause) {
        return new FlinkJobException(ErrorType.STOP_ERROR,
            "Failed to stop job: " + jobId, cause);
    }

    public static FlinkJobException networkError(String operation, Throwable cause) {
        return new FlinkJobException(ErrorType.NETWORK_ERROR,
            "Network error during " + operation, cause);
    }
}
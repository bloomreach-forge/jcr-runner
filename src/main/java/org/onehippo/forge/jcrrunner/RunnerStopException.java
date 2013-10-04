package org.onehippo.forge.jcrrunner;

/**
 * Exception which can be thrown by plugins to early stop the visiting cleanly.
 */
public class RunnerStopException extends RuntimeException {

    private static final long serialVersionUID = -4204063553726143782L;

    /**
     * Constructs a new instance of this class with <code>null</code> as its
     * detail message.
     */
    public RunnerStopException() {
        super();
    }

    /**
     * Constructs a new instance of this class with the specified detail
     * message.
     *
     * @param message the detail message. The detail message is saved for later
     *                retrieval by the {@link #getMessage()} method.
     */
    public RunnerStopException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of this class with the specified detail message
     * and root cause.
     *
     * @param message   the detail message. The detail message is saved for later
     *                  retrieval by the {@link #getMessage()} method.
     * @param rootCause root failure cause
     */
    public RunnerStopException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    /**
     * Constructs a new instance of this class with the specified root cause.
     *
     * @param rootCause root failure cause
     */
    public RunnerStopException(Throwable rootCause) {
        super(rootCause);
    }
}

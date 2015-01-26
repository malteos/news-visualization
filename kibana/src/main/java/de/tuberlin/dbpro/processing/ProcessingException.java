package de.tuberlin.dbpro.processing;

public class ProcessingException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -2827324332894415000L;

    /**
     *
     */
    public ProcessingException() {
    }

    /**
     * @param message
     */
    public ProcessingException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ProcessingException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

}

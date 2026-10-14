package agora.exceptions;

public class UnknownUserTypeException extends RuntimeException {
    public UnknownUserTypeException() {}

    public UnknownUserTypeException(String message) {
        super(message);
    }
}

package agora.exceptions;

public class AgoraIllegalArgumentException extends RuntimeException {

  public AgoraIllegalArgumentException(String message, Throwable cause) {
    super(message, cause);
  }

  public AgoraIllegalArgumentException(String message) {
        super(message);
    }
}

package agora.exceptions;

public class ClientDisconnectedException extends RuntimeException {

  public ClientDisconnectedException() {}

  public ClientDisconnectedException(String message) {
        super(message);
    }

}

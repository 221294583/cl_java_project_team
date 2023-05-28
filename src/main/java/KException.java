public class KException extends Exception{
    public KException() {
        super();
    }

    public KException(String message) {
        super(message);
    }

    public KException(String message, Throwable cause) {
        super(message, cause);
    }

    public KException(Throwable cause) {
        super(cause);
    }
}

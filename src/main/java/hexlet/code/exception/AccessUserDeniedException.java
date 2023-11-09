package hexlet.code.exception;

public class AccessUserDeniedException extends RuntimeException {
        public AccessUserDeniedException(String message) {
            super(message);
        }
}

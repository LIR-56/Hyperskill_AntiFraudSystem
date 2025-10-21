package antifraud.ex;

import org.springframework.http.HttpStatus;

public class WrongTransactionException extends RuntimeException {
    public WrongTransactionException() {
    }
}

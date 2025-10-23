package antifraud;

import antifraud.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AntifraudExceptionHandler {

    @ExceptionHandler({DuplicateUserException.class, DuplicateIpException.class,
            DuplicateStolenCardException.class, SameUserRoleException.class})
    public ResponseEntity<String> duplicateException() {
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, WrongIpFormatException.class,
            WrongCardNumberException.class, EmptyPasswordException.class,
             WrongUserRoleException.class, AttemptToChangeAdministratorRoleException.class})
    public ResponseEntity<String> validationException() {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({UserNotFoundException.class, SuspiciousIPNotFoundException.class,
            StolenCardNotFoundException.class})
    public ResponseEntity<String> notFoundException() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}

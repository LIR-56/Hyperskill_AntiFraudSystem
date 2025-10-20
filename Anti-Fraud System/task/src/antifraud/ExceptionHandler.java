package antifraud;

import antifraud.exceptions.DuplicateUserException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionHandler {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @org.springframework.web.bind.annotation.ExceptionHandler(DuplicateUserException.class)
    @ResponseBody
    public ResponseEntity<String> duplicateUser() {
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }


    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> validationError() {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}

package antifraud.transaction;

import jakarta.validation.Valid;
import org.hibernate.validator.constraints.CreditCardNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TransactionController {

    private final static StatusWrapper allowed = new StatusWrapper(TransactionValidity.ALLOWED, "none");

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/api/antifraud/transaction")
    public StatusWrapper checkTransaction(@RequestBody @Valid Transaction transaction) {
        var results = transactionService.checkTransaction(transaction);
        if (!results.getFirst().isEmpty()) {
            return new StatusWrapper(TransactionValidity.PROHIBITED, String.join(", ", results.getFirst()));
        }
        if (!results.getLast().isEmpty()) {
            return new StatusWrapper(TransactionValidity.MANUAL_PROCESSING, String.join(", ", results.getLast()));
        }
        return allowed;
    }

    @PutMapping("/api/antifraud/transaction")
    public Transaction addFeedback(@RequestBody Feedback feedback) {
        return transactionService.addFeedback(feedback.transactionId(), feedback.feedback());
    }

    @GetMapping("/api/antifraud/history")
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/api/antifraud/history/{number}")
    public List<Transaction> getTransaction(@PathVariable @CreditCardNumber String number) {
        return transactionService.getTransaction(number);
    }


    public record Feedback(long transactionId, TransactionValidity feedback) { }
    public record StatusWrapper(TransactionValidity result, String info) { }
}

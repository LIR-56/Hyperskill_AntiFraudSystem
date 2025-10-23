package antifraud.transaction;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {

    private final static StatusWrapper allowed = new StatusWrapper(TransactionType.ALLOWED, "none");

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/api/antifraud/transaction")
    public StatusWrapper checkTransaction(@RequestBody @Valid Transaction transaction) {
        var results = transactionService.checkTransaction(transaction);
        if (!results.getFirst().isEmpty()) {
            return new StatusWrapper(TransactionType.PROHIBITED, String.join(", ", results.getFirst()));
        }
        if (!results.getLast().isEmpty()) {
            return new StatusWrapper(TransactionType.MANUAL_PROCESSING, String.join(", ", results.getLast()));
        }
        return allowed;
    }

    public record StatusWrapper(TransactionType result, String info) { }
}

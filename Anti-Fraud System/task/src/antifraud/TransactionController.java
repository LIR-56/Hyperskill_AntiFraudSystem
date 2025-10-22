package antifraud;

import antifraud.ex.WrongTransactionException;
import antifraud.ip.SuspiciousIPService;
import antifraud.stolencard.StolenCardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.CreditCardNumber;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@RestController
public class TransactionController {

    public static StatusWrapper allowed = new StatusWrapper(TransactionType.ALLOWED, "none");
    public static StatusWrapper manualProcessing = new StatusWrapper(TransactionType.MANUAL_PROCESSING, "amount");

    private final SuspiciousIPService suspiciousIPService;
    private final StolenCardService stolenCardService;

    public TransactionController(SuspiciousIPService suspiciousIPService, StolenCardService stolenCardService) {
        this.suspiciousIPService = suspiciousIPService;
        this.stolenCardService = stolenCardService;
    }

    @PostMapping("/api/antifraud/transaction")
    public StatusWrapper checkTransaction(@RequestBody @Valid InputWrapper transaction) {
        List<String> errors = new LinkedList<>();
        if (suspiciousIPService.isIpSuspicious(transaction.ip)) {
            errors.addFirst("ip");
        }
        if (stolenCardService.isCardStolen(transaction.number)) {
            errors.addFirst("card-number");
        }
        if (transaction.amount() > 0) {
            if (transaction.amount() <= 200) {
                if ( errors.isEmpty()) {
                    return allowed;
                }
            } else if (transaction.amount() <= 1500) {
                if (errors.isEmpty()) {
                    return manualProcessing;
                }
            } else {
                errors.addFirst("amount");
            }
        } else {
            throw new WrongTransactionException();
        }
        return new StatusWrapper(TransactionType.PROHIBITED, String.join(", ", errors));
    }

    public record InputWrapper(long amount,
                               @NotNull
                               @Pattern(regexp = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")
                               String ip,
                               @CreditCardNumber String number) { }
    public record StatusWrapper(TransactionType result, String info) { }
}

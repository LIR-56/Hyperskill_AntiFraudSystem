package antifraud.transaction;

import antifraud.exceptions.TransactionsForCreditCardNotFound;
import antifraud.exceptions.IncorrectFeedbackException;
import antifraud.exceptions.FeedbackAlreadyGivenException;
import antifraud.exceptions.TransactionNotFoundException;
import antifraud.ip.SuspiciousIPService;
import antifraud.stolencard.StolenCardService;
import org.hibernate.validator.constraints.CreditCardNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final SuspiciousIPService suspiciousIPService;
    private final StolenCardService stolenCardService;
    private final TransactionRepository transactionRepository;

    private static long MAX_ALLOWED = 200;
    private static long MAX_MANUAL = 1500;

    @Autowired
    public TransactionService(SuspiciousIPService suspiciousIPService, StolenCardService stolenCardService, TransactionRepository transactionRepository) {
        this.suspiciousIPService = suspiciousIPService;
        this.stolenCardService = stolenCardService;
        this.transactionRepository = transactionRepository;
    }

    public List<List<String>> checkTransaction(Transaction transaction) {
        List<String> prohibitReasons = new LinkedList<>();
        List<String> manualProcessingReasons = new LinkedList<>();
        var hourAgo = transaction.getDate().minusHours(1);
        Map<Boolean, List<Transaction>> oldTransactions = transactionRepository.findAllByNumber(transaction.getNumber())
                .stream()
                .collect(Collectors.partitioningBy(x -> x.date.isAfter(hourAgo) && x.date.isBefore(transaction.getDate())));
        //oldTransactions.get(false).forEach(transactionRepository::delete); //cleaning old ones
        var regions = oldTransactions.get(true)
                .stream()
                .map(Transaction::getRegion)
                .collect(Collectors.toSet());
        regions.add(transaction.getRegion());

        if (regions.size() == 3) {
            manualProcessingReasons.addFirst("region-correlation");
        }
        if (regions.size() > 3) {
            prohibitReasons.addFirst("region-correlation");
        }

        var ips = oldTransactions.get(true)
                .stream()
                .map(Transaction::getIp)
                .collect(Collectors.toSet());
        ips.add(transaction.getIp());

        if (ips.size() == 3) {
            manualProcessingReasons.addFirst("ip-correlation");
        }
        if (ips.size() > 3) {
            prohibitReasons.addFirst("ip-correlation");
        }
        if (suspiciousIPService.isIpSuspicious(transaction.getIp())) {
            prohibitReasons.addFirst("ip");
        }
        if (stolenCardService.isCardStolen(transaction.getNumber())) {
            prohibitReasons.addFirst("card-number");
        }
        if (transaction.getAmount() <= MAX_MANUAL && transaction.getAmount() > MAX_ALLOWED) {
             manualProcessingReasons.addFirst("amount");
        } else if (transaction.getAmount() > MAX_MANUAL){
            prohibitReasons.addFirst("amount");
        }
        if (!prohibitReasons.isEmpty()) {
            transaction.setValidity(TransactionValidity.PROHIBITED);
        } else if (!manualProcessingReasons.isEmpty()) {
            transaction.setValidity(TransactionValidity.MANUAL_PROCESSING);
        } else {
            transaction.setValidity(TransactionValidity.ALLOWED);
        }
        transactionRepository.save(transaction);
        return List.of(prohibitReasons, manualProcessingReasons);

    }

    public Transaction addFeedback(long id, TransactionValidity feedback) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(TransactionNotFoundException::new);
        if (transaction.feedback != null) {
            throw new FeedbackAlreadyGivenException();
        }
        if (feedback == transaction.validity) {
            throw new IncorrectFeedbackException();
        }
        if (transaction.validity == TransactionValidity.ALLOWED) {
            MAX_ALLOWED = (long) Math.ceil(MAX_ALLOWED * 0.8 - 0.2 * transaction.getAmount());
            if (feedback == TransactionValidity.PROHIBITED) {
                MAX_MANUAL = (long) Math.ceil(MAX_MANUAL * 0.8 - 0.2 * transaction.getAmount());
            }
        } else if (transaction.validity == TransactionValidity.MANUAL_PROCESSING) {
            if (feedback == TransactionValidity.ALLOWED) {
                MAX_ALLOWED = (long) Math.ceil(MAX_ALLOWED * 0.8 + 0.2 * transaction.getAmount());
            } else if (feedback == TransactionValidity.PROHIBITED) {
                MAX_MANUAL = (long) Math.ceil(MAX_MANUAL * 0.8 - 0.2 * transaction.getAmount());
            }
        } else if (transaction.validity == TransactionValidity.PROHIBITED) {
            MAX_MANUAL = (long) Math.ceil(MAX_MANUAL * 0.8 + 0.2 * transaction.getAmount());
            if (feedback == TransactionValidity.ALLOWED) {
                MAX_ALLOWED = (long) Math.ceil(MAX_ALLOWED * 0.8 + 0.2 * transaction.getAmount());
            }
        }
        transaction.setFeedback(feedback);
        transactionRepository.save(transaction);
        return transaction;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByIdAsc();
    }

    public List<Transaction> getTransaction(@CreditCardNumber String creditCardNumber) {
        var result = transactionRepository.findAllByNumber(creditCardNumber);
        if (result.isEmpty()) {
            throw new TransactionsForCreditCardNotFound();
        }
        return result;
    }
}

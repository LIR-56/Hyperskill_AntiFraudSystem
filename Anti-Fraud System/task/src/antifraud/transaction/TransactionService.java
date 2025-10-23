package antifraud.transaction;

import antifraud.ip.SuspiciousIPService;
import antifraud.stolencard.StolenCardService;
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
        if (transaction.getAmount() <= 1500 && transaction.getAmount() > 200) {
             manualProcessingReasons.addFirst("amount");
        } else if (transaction.getAmount() > 1500){
            prohibitReasons.addFirst("amount");
        }
        transactionRepository.save(transaction);
        return List.of(prohibitReasons, manualProcessingReasons);

    }
}

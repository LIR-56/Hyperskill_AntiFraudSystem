package antifraud.stolencard;

import jakarta.validation.Valid;
import org.hibernate.validator.constraints.CreditCardNumber;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class StolenCardController {

    private final StolenCardService stolenCardService;

    public StolenCardController(StolenCardService stolenCardService) {
        this.stolenCardService = stolenCardService;
    }

    @PostMapping("/api/antifraud/stolencard")
    public StolenCard addCard(@RequestBody @Valid StolenCard stolenCard) {
        return stolenCardService.addStolenCard(stolenCard);
    }

    @GetMapping("/api/antifraud/stolencard")
    public List<StolenCard> getAllCards() {
        return stolenCardService.getAll();
    }

    @DeleteMapping("/api/antifraud/stolencard/{number}")
    public Map<String, String> deleteStolenCard(@PathVariable @CreditCardNumber @Valid String number) {
        stolenCardService.deleteStolenCard(number);
        return Map.of("status", String.format("Card %s successfully removed!", number));
    }
}

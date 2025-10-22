package antifraud.stolencard;

import antifraud.exceptions.DuplicateStolenCardException;
import antifraud.exceptions.StolenCardNotFoundException;
import antifraud.exceptions.WrongCardNumberException;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;

import java.util.List;

@Service
public class StolenCardService {

    private final StolenCardRepository stolenCardRepository;
    private final Validator validator;

    public StolenCardService(StolenCardRepository stolenCardRepository, Validator validator) {
        this.stolenCardRepository = stolenCardRepository;
        this.validator = validator;
    }

    public StolenCard addStolenCard(StolenCard card) {
        var x = stolenCardRepository.findByNumber(card.getNumber());
        if (x != null) {
            throw new DuplicateStolenCardException();
        }
        return stolenCardRepository.save(card);
    }

    public void deleteStolenCard(String number) {
        var stolenCard = new StolenCard();
        stolenCard.setNumber(number);
        var errors = validator.validateObject(stolenCard);
        if (errors.hasErrors()) {
            throw new WrongCardNumberException();
        }
        stolenCard = stolenCardRepository.findByNumber(number);
        if (stolenCard == null) {
            throw new StolenCardNotFoundException();
        }
        stolenCardRepository.delete(stolenCard);
    }

    public List<StolenCard> getAll() {
        return stolenCardRepository.findAll();
    }

    public boolean isCardStolen(String number) {
        return stolenCardRepository.findByNumber(number) != null;
    }
}

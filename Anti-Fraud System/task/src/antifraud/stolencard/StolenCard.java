package antifraud.stolencard;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.CreditCardNumber;

@Entity
@Table(name = "stolen_cards", indexes = @Index(columnList = "card_number"))
public class StolenCard {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "card_id")
    private long id;

    @Column(name = "card_number", unique = true)
    @Nonnull
    @NotBlank
    @CreditCardNumber
    private String number;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Nonnull
    public String getNumber() {
        return number;
    }

    public void setNumber(@Nonnull String number) {
        this.number = number;
    }
}

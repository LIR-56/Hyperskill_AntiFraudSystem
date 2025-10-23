package antifraud.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.CreditCardNumber;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {@Index(columnList = "card_number")})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "transaction_id")
    private long id;

    @Min(1)
    @Column(name = "amount")
    private long amount;

    @NotNull
    @Pattern(regexp = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")
    @Column(name = "ip_addr")
    private String ip;

    @CreditCardNumber
    @Column(name = "card_number")
    private  String number;

    @NotNull
    @Column(name = "region_code")
    private TransactionRegion region;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-ddThh:mm:ss")
    LocalDateTime date;

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public TransactionRegion getRegion() {
        return region;
    }

    public void setRegion(TransactionRegion region) {
        this.region = region;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
